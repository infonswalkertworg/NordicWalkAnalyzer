@file:Suppress("unused")

package com.nordicwalk.feature.video.util

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.nordicwalk.feature.video.presentation.PosePoint

class PoseAnalyzerUtil(context: Context) {
    private var poseLandmarker: PoseLandmarker? = null

    init {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_heavy.task")
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumPoses(1)
                .setMinPoseDetectionConfidence(0.5f)
                .setMinPosePresenceConfidence(0.5f)
                .setMinTrackingConfidence(0.5f)
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun extractPosePoints(bitmap: Bitmap): List<PosePoint> {
        return try {
            val landmarker = poseLandmarker ?: return emptyList()
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = landmarker.detect(mpImage)
            val landmarks = result.landmarks().firstOrNull() ?: return emptyList()

            landmarks.map { landmark ->
                // === 修正：適配新版 PosePoint 結構 (x, y 歸一化座標) ===
                PosePoint(
                    x = landmark.x(),
                    y = landmark.y(),
                    confidence = landmark.visibility().orElse(0f)
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }
}
