package com.nordicwalk.core.domain.model

import java.time.LocalDateTime

/**
 * Represents a single landmark (joint) detected in pose estimation
 */
data class PoseLandmark(
    val id: Int,
    val x: Float,  // Normalized X coordinate (0-1)
    val y: Float,  // Normalized Y coordinate (0-1)
    val z: Float,  // Depth coordinate
    val confidence: Float  // Confidence score (0-1)
) {
    val isVisible: Boolean get() = confidence > 0.5f
}

/**
 * Represents a complete pose frame with all landmarks
 */
data class PoseFrame(
    val id: Long,
    val timestamp: LocalDateTime,
    val landmarks: List<PoseLandmark>,
    val frameIndex: Int = 0,
    val width: Int = 0,
    val height: Int = 0
) {
    fun getLandmark(id: Int): PoseLandmark? = landmarks.find { it.id == id }
}

/**
 * Enum for different pose analysis directions
 */
enum class ViewDirection {
    FRONT,      // Front view (face forward)
    BACK,       // Back view
    LEFT,       // Left side view
    RIGHT       // Right side view
}

/**
 * Enum for capture sources
 */
enum class CaptureSource {
    CAMERA,     // Live camera feed
    VIDEO       // Imported video file
}

/**
 * Represents pose analysis metrics calculated from landmarks
 */
data class PoseMetrics(
    val frameId: Long,
    val timestamp: LocalDateTime,
    // Body angles (in degrees)
    val trunkTilt: Float = 0f,              // Forward/backward lean
    val neckAngle: Float = 0f,              // Neck flexion
    val shoulderAngle: Float = 0f,          // Shoulder height difference
    
    // Arm swing metrics
    val leftArmSwingForward: Float = 0f,    // Left arm forward angle
    val leftArmSwingBackward: Float = 0f,   // Left arm backward angle
    val rightArmSwingForward: Float = 0f,   // Right arm forward angle
    val rightArmSwingBackward: Float = 0f,  // Right arm backward angle
    
    // Stride metrics
    val stepLength: Float = 0f,              // Step length in cm
    val strideLength: Float = 0f,            // Stride length in cm
    val stepWidth: Float = 0f,               // Distance between feet
    
    // Center of mass
    val comHeight: Float = 0f,               // Center of mass height
    val comDisplacement: Float = 0f,         // Vertical COM displacement
    
    // Hand/Pole metrics
    val leftHandOpen: Boolean = false,       // Is left hand open?
    val rightHandOpen: Boolean = false,      // Is right hand open?
    val leftPoleAngle: Float = 0f,           // Left pole angle from vertical
    val rightPoleAngle: Float = 0f,          // Right pole angle from vertical
    
    // Overall quality
    val overallConfidence: Float = 0f        // Average confidence of all landmarks
)

/**
 * Represents a complete analysis session
 */
data class AnalysisSession(
    val id: Long,
    val studentId: Long,
    val direction: ViewDirection,
    val captureSource: CaptureSource,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime? = null,
    val frames: List<PoseFrame> = emptyList(),
    val metrics: List<PoseMetrics> = emptyList(),
    val violations: List<PostureViolation> = emptyList()
)

/**
 * Represents a posture violation or form error
 */
data class PostureViolation(
    val id: Long,
    val sessionId: Long,
    val frameId: Long,
    val violationType: ViolationType,
    val severity: ViolationSeverity,
    val description: String,
    val suggestion: String,
    val timestamp: LocalDateTime
)

/**
enum for violation types
 */
enum class ViolationType {
    EXCESSIVE_TRUNK_TILT,
    INSUFFICIENT_ARM_SWING,
    POOR_HAND_POSITION,
    IMPROPER_POLE_ANGLE,
    UNEVEN_STRIDE,
    POOR_POSTURE,
    OTHER
}

/**
 * Enum for violation severity
 */
enum class ViolationSeverity {
    CRITICAL,   // Critical form error
    WARNING,    // Should be corrected
    INFO        // Minor suggestion
}

/**
 * Real-time pose detection result
 */
data class PoseDetectionResult(
    val isSuccess: Boolean,
    val frame: PoseFrame? = null,
    val metrics: PoseMetrics? = null,
    val confidence: Float = 0f,
    val errorMessage: String? = null,
    val processingTimeMs: Long = 0
)
