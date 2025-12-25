package com.nordicwalk.feature.analysis.domain

import com.nordicwalk.core.domain.model.PoseFrame
import com.nordicwalk.core.domain.model.PoseLandmark
import com.nordicwalk.core.domain.model.PoseMetrics
import com.nordicwalk.core.domain.model.PostureViolation
import com.nordicwalk.core.domain.model.ViolationType
import com.nordicwalk.core.domain.model.ViolationSeverity
import com.nordicwalk.feature.videoanalysis.pose.MediaPipePoseDetector
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Comprehensive posture analysis engine for Nordic Walk technique evaluation
 * Analyzes body mechanics from pose detection results
 */
@Singleton
class PostureAnalysisEngine @Inject constructor(
    private val poseDetector: MediaPipePoseDetector?
) {

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

        // Check stride consistency
        if (metrics.stepLength < 0.3f) {
            violations.add(
                PostureViolation(
                    id = System.currentTimeMillis() + 4,
                    sessionId = sessionId,
                    frameId = frame.id,
                    violationType = ViolationType.UNEVEN_STRIDE,
                    severity = ViolationSeverity.INFO,
                    description = "Short stride length: ${String.format("%.2f", metrics.stepLength)}m",
                    suggestion = "Increase stride length. Aim for 0.6-1.0m stride length.",
                    timestamp = metrics.timestamp
                )
            )
        }

        // Check overall posture quality
        if (metrics.overallConfidence < 0.6f) {
            violations.add(
                PostureViolation(
                    id = System.currentTimeMillis() + 5,
                    sessionId = sessionId,
                    frameId = frame.id,
                    violationType = ViolationType.POOR_POSTURE,
                    severity = ViolationSeverity.INFO,
                    description = "Low pose detection confidence: ${String.format("%.1f", metrics.overallConfidence * 100)}%",
                    suggestion = "Ensure full body is visible in frame. Better lighting may help.",
                    timestamp = metrics.timestamp
                )
            )
        }

        return violations
    }

    /**
     * Calculate trunk tilt angle (forward lean)
     */
    private fun calculateTrunkTilt(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        val leftShoulder = landmarks.find { it.id == MediaPipePoseDetector.LEFT_SHOULDER } ?: return 0f
        val rightShoulder = landmarks.find { it.id == MediaPipePoseDetector.RIGHT_SHOULDER } ?: return 0f
        val leftHip = landmarks.find { it.id == MediaPipePoseDetector.LEFT_HIP } ?: return 0f
        val rightHip = landmarks.find { it.id == MediaPipePoseDetector.RIGHT_HIP } ?: return 0f

        val shoulderY = (leftShoulder.y + rightShoulder.y) / 2
        val hipY = (leftHip.y + rightHip.y) / 2
        val shoulderX = (leftShoulder.x + rightShoulder.x) / 2
        val hipX = (leftHip.x + rightHip.x) / 2

        // Vertical line is 90 degrees, calculate deviation
        val deltaY = shoulderY - hipY
        val deltaX = shoulderX - hipX
        
        val angle = Math.toDegrees(Math.atan2(deltaX.toDouble(), deltaY.toDouble())).toFloat()
        return angle
    }

    /**
     * Calculate neck angle
     */
    private fun calculateNeckAngle(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        // Placeholder implementation
        return 0f
    }

    /**
     * Calculate shoulder height difference (lateral tilt)
     */
    private fun calculateShoulderAngle(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        val leftShoulder = landmarks.find { it.id == MediaPipePoseDetector.LEFT_SHOULDER } ?: return 0f
        val rightShoulder = landmarks.find { it.id == MediaPipePoseDetector.RIGHT_SHOULDER } ?: return 0f
        
        return abs(leftShoulder.y - rightShoulder.y) * frame.height
    }

    /**
     * Calculate arm swing angle
     */
    private fun calculateArmAngle(
        landmarks: List<PoseLandmark>,
        frame: PoseFrame,
        isLeft: Boolean,
        isForward: Boolean
    ): Float {
        val shoulderId = if (isLeft) MediaPipePoseDetector.LEFT_SHOULDER else MediaPipePoseDetector.RIGHT_SHOULDER
        val elbowId = if (isLeft) MediaPipePoseDetector.LEFT_ELBOW else MediaPipePoseDetector.RIGHT_ELBOW
        val wristId = if (isLeft) MediaPipePoseDetector.LEFT_WRIST else MediaPipePoseDetector.RIGHT_WRIST

        val shoulder = landmarks.find { it.id == shoulderId } ?: return 0f
        val elbow = landmarks.find { it.id == elbowId } ?: return 0f
        val wrist = landmarks.find { it.id == wristId } ?: return 0f

        // Calculate angle using poseDetector if available
        return poseDetector?.calculateAngle(shoulder, elbow, wrist, frame.width, frame.height) ?: 0f
    }

    /**
     * Calculate step length
     */
    private fun calculateStepLength(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        val leftAnkle = landmarks.find { it.id == MediaPipePoseDetector.LEFT_ANKLE } ?: return 0f
        val rightAnkle = landmarks.find { it.id == MediaPipePoseDetector.RIGHT_ANKLE } ?: return 0f

        val distance = poseDetector?.calculateLandmarkDistance(
            leftAnkle, rightAnkle, frame.width, frame.height
        ) ?: return 0f

        // Assume frame height is roughly 1.8m, scale accordingly
        return (distance / frame.height) * 1.8f
    }

    /**
     * Calculate stride length (distance between same foot contacts)
     */
    private fun calculateStrideLength(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        // Placeholder - would track foot position over time
        return 0f
    }

    /**
     * Calculate step width (lateral distance between feet)
     */
    private fun calculateStepWidth(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        val leftAnkle = landmarks.find { it.id == MediaPipePoseDetector.LEFT_ANKLE } ?: return 0f
        val rightAnkle = landmarks.find { it.id == MediaPipePoseDetector.RIGHT_ANKLE } ?: return 0f

        val lateralDistance = abs(leftAnkle.x - rightAnkle.x) * frame.width
        
        // Assume frame width is roughly 0.6m at ankle level
        return (lateralDistance / frame.width) * 0.6f
    }

    /**
     * Calculate center of mass height
     */
    private fun calculateCOMHeight(landmarks: List<PoseLandmark>, frame: PoseFrame): Float {
        // COM is approximately at 55% of body height above ground
        val nose = landmarks.find { it.id == MediaPipePoseDetector.NOSE } ?: return 0f
        val leftAnkle = landmarks.find { it.id == MediaPipePoseDetector.LEFT_ANKLE } ?: return 0f

        val bodyHeight = abs(nose.y - leftAnkle.y) * frame.height
        return bodyHeight * 0.55f
    }

    /**
     * Calculate center of mass displacement (vertical bobbing)
     */
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

    /**
     * Detect if hand is open (wrist landmarks spread)
     */
    private fun isHandOpen(landmarks: List<PoseLandmark>, isLeft: Boolean): Boolean {
        val wristId = if (isLeft) MediaPipePoseDetector.LEFT_WRIST else MediaPipePoseDetector.RIGHT_WRIST
        val indexId = if (isLeft) 19 else 20  // Index finger landmark

        val wrist = landmarks.find { it.id == wristId } ?: return false
        val index = landmarks.find { it.id == indexId } ?: return false

        // If index finger is far from wrist, hand is open
        val distance = abs(wrist.x - index.x) + abs(wrist.y - index.y)
        return distance > 0.15f
    }

    /**
     * Calculate pole angle from vertical
     */
    private fun calculatePoleAngle(
        landmarks: List<PoseLandmark>,
        frame: PoseFrame,
        isLeft: Boolean
    ): Float {
        val shoulderId = if (isLeft) MediaPipePoseDetector.LEFT_SHOULDER else MediaPipePoseDetector.RIGHT_SHOULDER
        val wristId = if (isLeft) MediaPipePoseDetector.LEFT_WRIST else MediaPipePoseDetector.RIGHT_WRIST

        val shoulder = landmarks.find { it.id == shoulderId } ?: return 0f
        val wrist = landmarks.find { it.id == wristId } ?: return 0f

        // Angle from vertical
        val deltaX = wrist.x - shoulder.x
        val deltaY = wrist.y - shoulder.y

        val angle = Math.toDegrees(Math.atan2(deltaX.toDouble(), deltaY.toDouble())).toFloat()
        return angle
    }
}
