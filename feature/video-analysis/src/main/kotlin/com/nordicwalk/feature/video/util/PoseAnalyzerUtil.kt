package com.nordicwalk.feature.video.util

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import com.nordicwalk.feature.video.presentation.PosePoint

/**
 * 利用 MediaPipe 提取妻態关节点
 */
class PoseAnalyzerUtil(context: Context) {
    private var poseLandmarker: PoseLandmarker? = null
    
    init {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_full.tflite")
                .build()
            
            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(com.google.mediapipe.tasks.vision.core.RunningMode.IMAGE)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(0.3f)
                .setMinPosePresenceConfidence(0.3f)
                .setMinTrackingConfidence(0.3f)
                .build()
            
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun extractPosePoints(bitmap: Bitmap): List<PosePoint> {
        return try {
            if (poseLandmarker == null) return emptyList()
            
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = poseLandmarker?.detect(mpImage)
            
            val points = mutableListOf<PosePoint>()
            
            result?.landmarks()?.forEach { landmarks ->
                landmarks.forEachIndexed { index, landmark ->
                    // 将正一化坐标转换为像素坐标
                    val x = landmark.x() * bitmap.width
                    val y = landmark.y() * bitmap.height
                    // Fix: Handle Optional<Float> from presence()
                    val confidence = landmark.presence().orElse(0f)
                    
                    points.add(
                        PosePoint(
                            x = x,
                            y = y,
                            confidence = confidence,
                            label = getKeyPointName(index)
                        )
                    )
                }
            }
            
            points
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
    
    private fun getKeyPointName(index: Int): String {
        return when (index) {
            0 -> "nose"
            1 -> "left_eye_inner"
            2 -> "left_eye"
            3 -> "left_eye_outer"
            4 -> "right_eye_inner"
            5 -> "right_eye"
            6 -> "right_eye_outer"
            7 -> "left_ear"
            8 -> "right_ear"
            9 -> "mouth_left"
            10 -> "mouth_right"
            11 -> "left_shoulder"
            12 -> "right_shoulder"
            13 -> "left_elbow"
            14 -> "right_elbow"
            15 -> "left_wrist"
            16 -> "right_wrist"
            17 -> "left_pinky"
            18 -> "right_pinky"
            19 -> "left_index"
            20 -> "right_index"
            21 -> "left_thumb"
            22 -> "right_thumb"
            23 -> "left_hip"
            24 -> "right_hip"
            25 -> "left_knee"
            26 -> "right_knee"
            27 -> "left_ankle"
            28 -> "right_ankle"
            29 -> "left_heel"
            30 -> "right_heel"
            31 -> "left_foot_index"
            32 -> "right_foot_index"
            else -> "unknown"
        }
    }
}
