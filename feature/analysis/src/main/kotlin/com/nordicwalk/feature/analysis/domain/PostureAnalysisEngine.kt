package com.nordicwalk.feature.analysis.domain

import com.nordicwalk.core.domain.model.PoseFrame
import com.nordicwalk.core.domain.model.PoseLandmark
import com.nordicwalk.core.domain.model.PoseMetrics
import com.nordicwalk.core.domain.model.PostureViolation
import com.nordicwalk.core.domain.model.ViolationType
import com.nordicwalk.core.domain.model.ViolationSeverity
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.atan2

/**
 * Comprehensive posture analysis engine for Nordic Walk technique evaluation
 * Analyzes body mechanics from pose detection results
 */
@Singleton
class PostureAnalysisEngine @Inject constructor() {

    companion object {
        // MediaPipe Pose landmark IDs
        const val NOSE = 0
        const val LEFT_SHOULDER = 11
        const val RIGHT_SHOULDER = 12
        const val LEFT_ELBOW = 13
        const val RIGHT_ELBOW = 14
        const val LEFT_WRIST = 15
        const val RIGHT_WRIST = 16
        const val LEFT_HIP = 23
        const val RIGHT_HIP = 24
        const val LEFT_KNEE = 25
        const val RIGHT_KNEE = 26
        const val LEFT_ANKLE = 27
        const val RIGHT_ANKLE = 28
    }

    /**
     * Analyze pose frame and calculate metrics
     */
    fun analyzeFrame(
        frame: PoseFrame,
        previousFrame: PoseFrame? = null
    ): PoseMetrics {
        val landmarks = frame.landmarks

        return PoseMetrics(
            frameId = frame.id,
            timestamp = frame.timestamp,
            
            // Calculate postural angles
            trunkTilt = calculateTrunkTilt(landmarks, frame),
            neckAngle = calculateNeckAngle(landmarks, frame),
            shoulderAngle = calculateShoulderAngle(landmarks, frame),
            
            // Arm swing metrics
            leftArmSwingForward = calculateArmAngle(landmarks, frame, isLeft = true, isForward = true),
            leftArmSwingBackward = calculateArmAngle(landmarks, frame, isLeft = true, isForward = false),
            rightArmSwingForward = calculateArmAngle(landmarks, frame, isLeft = false, isForward = true),
            rightArmSwingBackward = calculateArmAngle(landmarks, frame, isLeft = false, isForward = false),
            
            // Stride metrics
            stepLength = calculateStepLength(landmarks, frame),
            strideLength = calculateStrideLength(landmarks, frame),
            stepWidth = calculateStepWidth(landmarks, frame),
            
            // Center of mass
            comHeight = calculateCOMHeight(landmarks, frame),
            comDisplacement = calculateCOMDisplacement(landmarks, previousFrame, frame),
            
            // Hand/Pole position
            leftHandOpen = isHandOpen(landmarks, isLeft = true),
            rightHandOpen = isHandOpen(landmarks, isLeft = false),
            leftPoleAngle = calculatePoleAngle(landmarks, frame, isLeft = true),
            rightPoleAngle = calculatePoleAngle(landmarks, frame, isLeft = false),
            
            // Overall confidence
            overallConfidence = landmarks.map { it.confidence }.average().toFloat()
        )
    }

    /**
     * Detect posture violations in a frame
     */
    fun detectViolations(
        frame: PoseFrame,
        metrics: PoseMetrics,
        sessionId: Long
    ): List<PostureViolation> {
        val violations = mutableListOf<PostureViolation>()

        // Check trunk tilt
        if (abs(metrics.trunkTilt) > 25f) {
            violations.add(
                PostureViolation(
                    id = System.currentTimeMillis(),
                    sessionId = sessionId,
                    frameId = frame.id,
                    violationType = ViolationType.EXCESSIVE_TRUNK_TILT,
                    severity = if (abs(metrics.trunkTilt) > 35f) ViolationSeverity.CRITICAL else ViolationSeverity.WARNING,
                    description = "Trunk tilt: ${String.format("%.1f", metrics.trunkTilt)}°",
                    suggestion = "Maintain upright posture. Lean forward only 5-15° from vertical.",
                    timestamp = metrics.timestamp
                )
            )
        }

        // Check arm swing
        val avgArmSwing = (
            metrics.leftArmSwingForward + metrics.rightArmSwingForward +
            metrics.leftArmSwingBackward + metrics.rightArmSwingBackward
        ) / 4
        
        if (avgArmSwing < 30f) {
            violations.add(
                PostureViolation(
                    id = System.currentTimeMillis() + 1,
                    sessionId = sessionId,
                    frameId = frame.id,
                    violationType = ViolationType.INSUFFICIENT_ARM_SWING,
                    severity = ViolationSeverity.WARNING,
                    description = "Insufficient arm swing: ${String.format("%.1f", avgArmSwing)}°",
                    suggestion = "Increase arm swing amplitude. Swing arms 45-90° from vertical.",
                    timestamp = metrics.timestamp
                )
            )
        }

        // Check hand position
        if (metrics.leftHandOpen && metrics.rightHandOpen) {
            violations.add(
                PostureViolation(
                    id = System.currentTimeMillis() + 2,
                    sessionId = sessionId,
                    frameId = frame.id,
                    violationType = ViolationType.POOR_HAND_POSITION,
                    severity = ViolationSeverity.INFO,
                    description = "Both hands appear open",
                    suggestion = "Maintain grip on poles. Hands should be closed around pole handles.",
                    timestamp = metrics.timestamp
                )
            )
        }

        // Check pole angle
        if (abs(metrics.leftPoleAngle) > 60f || abs(metrics.rightPoleAngle) > 60f) {
            violations.add(
                PostureViolation(
                    id = System.currentTimeMillis() + 3,
                    sessionId = sessionId,
                    frameId = frame.id,
                    violationType = ViolationType.IMPROPER_POLE_ANGLE,
                    severity = ViolationSeverity.WARNING,
                    description = "Pole angle deviation",
                    suggestion = "Keep poles at 45-60° angle from ground. Push poles diagonally backward.",
                    timestamp = metrics.timestamp
                )
            )
        }

        return violations
    }

    // Helper function to calculate distance between two landmarks
    private fun calculateDistance(
        p1: PoseLandmark,
        p2: PoseLandmark,
        width: Int,
        height: Int
    ): Float {
        val dx = (p1.x - p2.x) * width
        val dy = (p1.y - p2.y) * height
        return sqrt(dx * dx + dy * dy)
    }

    // Helper function to calculate angle between three points
    private fun calculateAngle3Points(
        p1: PoseLandmark,
        p2: PoseLandmark,
        p3: PoseLandmark,
        width: Int,
        height: Int
    ): Float {
        val v1x = (p1.x - p2.x) * width
        val v1y = (p1.y - p2.y) * height
        val v2x = (p3.x - p2.x) * width
        val v2y = (p3.y - p2.y) * height

        val dot = v1x * v2x + v1y * v2y
        val len1 = sqrt(v1x * v1x + v1y * v1y)
        val len2 = sqrt(v2x * v2x + v2y * v2y)

        val angle = Math.toDegrees(Math.acos((dot / (len1 * len2)).toDouble())).toFloat()
        return angle
    }

    private fun calculateTrunkTilt(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        val leftShoulder = landmarks.find { it.id == LEFT_SHOULDER } ?: return 0f
        val rightShoulder = landmarks.find { it.id == RIGHT_SHOULDER } ?: return 0f
        val leftHip = landmarks.find { it.id == LEFT_HIP } ?: return 0f
        val rightHip = landmarks.find { it.id == RIGHT_HIP } ?: return 0f

        val shoulderY = (leftShoulder.y + rightShoulder.y) / 2
        val hipY = (leftHip.y + rightHip.y) / 2
        val shoulderX = (leftShoulder.x + rightShoulder.x) / 2
        val hipX = (leftHip.x + rightHip.x) / 2

        val deltaY = shoulderY - hipY
        val deltaX = shoulderX - hipX
        
        val angle = Math.toDegrees(atan2(deltaX.toDouble(), deltaY.toDouble())).toFloat()
        return angle
    }

    private fun calculateNeckAngle(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        return 0f  // Placeholder
    }

    private fun calculateShoulderAngle(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        val leftShoulder = landmarks.find { it.id == LEFT_SHOULDER } ?: return 0f
        val rightShoulder = landmarks.find { it.id == RIGHT_SHOULDER } ?: return 0f
        
        return abs(leftShoulder.y - rightShoulder.y) * frame.height
    }

    private fun calculateArmAngle(
        landmarks: List<PoseLandmark>,
        frame: PoseFrame,
        isLeft: Boolean,
        isForward: Boolean
    ): Float {
        val shoulderId = if (isLeft) LEFT_SHOULDER else RIGHT_SHOULDER
        val elbowId = if (isLeft) LEFT_ELBOW else RIGHT_ELBOW
        val wristId = if (isLeft) LEFT_WRIST else RIGHT_WRIST

        val shoulder = landmarks.find { it.id == shoulderId } ?: return 0f
        val elbow = landmarks.find { it.id == elbowId } ?: return 0f
        val wrist = landmarks.find { it.id == wristId } ?: return 0f

        return calculateAngle3Points(shoulder, elbow, wrist, frame.width, frame.height)
    }

    private fun calculateStepLength(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        val leftAnkle = landmarks.find { it.id == LEFT_ANKLE } ?: return 0f
        val rightAnkle = landmarks.find { it.id == RIGHT_ANKLE } ?: return 0f

        val distance = calculateDistance(leftAnkle, rightAnkle, frame.width, frame.height)
        return (distance / frame.height) * 1.8f
    }

    private fun calculateStrideLength(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        return 0f  // Placeholder
    }

    private fun calculateStepWidth(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        val leftAnkle = landmarks.find { it.id == LEFT_ANKLE } ?: return 0f
        val rightAnkle = landmarks.find { it.id == RIGHT_ANKLE } ?: return 0f

        val lateralDistance = abs(leftAnkle.x - rightAnkle.x) * frame.width
        return (lateralDistance / frame.width) * 0.6f
    }

    private fun calculateCOMHeight(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        val nose = landmarks.find { it.id == NOSE } ?: return 0f
        val leftAnkle = landmarks.find { it.id == LEFT_ANKLE } ?: return 0f

        val bodyHeight = abs(nose.y - leftAnkle.y) * frame.height
        return bodyHeight * 0.55f
    }

    private fun calculateCOMDisplacement(
        currentLandmarks: List<PoseLandmark>,
        previousFrame: PoseFrame?,
        currentFrame: PoseFrame
    ): Float {
        if (previousFrame == null) return 0f

        val currentCOM = calculateCOMHeight(currentLandmarks, currentFrame)
        val previousCOM = calculateCOMHeight(previousFrame.landmarks, previousFrame)

        return abs(currentCOM - previousCOM)
    }

    private fun isHandOpen(landmarks: List<PoseLandmark>, isLeft: Boolean): Boolean {
        val wristId = if (isLeft) LEFT_WRIST else RIGHT_WRIST
        val indexId = if (isLeft) 19 else 20

        val wrist = landmarks.find { it.id == wristId } ?: return false
        val index = landmarks.find { it.id == indexId } ?: return false

        val distance = abs(wrist.x - index.x) + abs(wrist.y - index.y)
        return distance > 0.15f
    }

    private fun calculatePoleAngle(
        landmarks: List<PoseLandmark>,
        frame: PoseFrame,
        isLeft: Boolean
    ): Float {
        val shoulderId = if (isLeft) LEFT_SHOULDER else RIGHT_SHOULDER
        val wristId = if (isLeft) LEFT_WRIST else RIGHT_WRIST

        val shoulder = landmarks.find { it.id == shoulderId } ?: return 0f
        val wrist = landmarks.find { it.id == wristId } ?: return 0f

        val deltaX = wrist.x - shoulder.x
        val deltaY = wrist.y - shoulder.y

        val angle = Math.toDegrees(atan2(deltaX.toDouble(), deltaY.toDouble())).toFloat()
        return angle
    }
}
