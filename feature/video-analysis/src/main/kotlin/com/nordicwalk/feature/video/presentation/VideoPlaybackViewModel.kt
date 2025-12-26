@file:Suppress("unused")

package com.nordicwalk.feature.video.presentation

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

// 改用歸一化座標 (0.0 ~ 1.0)，不依賴具體解析度
data class PosePoint(
    val x: Float,
    val y: Float,
    val confidence: Float
)

@HiltViewModel
class VideoPlaybackViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _posePoints = MutableStateFlow<List<PosePoint>>(emptyList())
    val posePoints: StateFlow<List<PosePoint>> = _posePoints.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)

    // 原子鎖：防止同時進行多個分析任務 (解決格放問題的關鍵)
    private val isProcessingFrame = AtomicBoolean(false)

    private var retriever: MediaMetadataRetriever? = null
    private var poseLandmarker: PoseLandmarker? = null

    private var lastAnalyzedTimeMs: Long = -1
    private var videoRotation = 0 // 儲存影片旋轉角度

    init {
        initializePoseLandmarker()
    }

    private fun initializePoseLandmarker() {
        try {
            // 使用 Lite 模型以獲得最快速度
            val modelPath = "pose_landmarker_lite.task"
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(modelPath)
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
            Log.e(TAG, "Error initializing PoseLandmarker", e)
        }
    }

    fun prepareVideo(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                retriever?.release()
                retriever = MediaMetadataRetriever()

                if (path.startsWith("content://")) {
                    retriever?.setDataSource(context, Uri.parse(path))
                } else {
                    retriever?.setDataSource(path)
                }

                // 讀取旋轉角度
                val rotationStr = retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                videoRotation = rotationStr?.toIntOrNull() ?: 0
                Log.d(TAG, "Video Rotation: $videoRotation")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to prepare retriever", e)
            }
        }
    }

    fun analyzeFrameAt(timeMs: Long) {
        // 1. 限制最小間隔 (避免過度頻繁請求)
        if (kotlin.math.abs(timeMs - lastAnalyzedTimeMs) < 30) return

        // 2. 關鍵優化：如果上一次分析還沒完成，直接放棄這次請求 (Drop Frame)
        // 這樣可以保證 UI 和播放器不會被卡住
        if (!isProcessingFrame.compareAndSet(false, true)) {
            return
        }

        lastAnalyzedTimeMs = timeMs
        _isAnalyzing.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val localRetriever = retriever ?: return@launch

                // 抓取原始圖片
                val rawBitmap = localRetriever.getFrameAtTime(
                    timeMs * 1000,
                    MediaMetadataRetriever.OPTION_CLOSEST
                )

                if (rawBitmap != null) {
                    // === 強制轉正邏輯 ===
                    var rotation = videoRotation.toFloat()
                    if (rotation == 0f && rawBitmap.width > rawBitmap.height) {
                        rotation = 90f
                    }

                    val finalBitmap = if (rotation != 0f) {
                        rotateBitmap(rawBitmap, rotation)
                    } else {
                        rawBitmap
                    }

                    detectPose(finalBitmap)
                } else {
                    _posePoints.value = emptyList()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing frame", e)
            } finally {
                _isAnalyzing.value = false
                isProcessingFrame.set(false) // 釋放鎖
            }
        }
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun detectPose(bitmap: Bitmap) {
        try {
            val landmarker = poseLandmarker ?: return
            val mpImage = BitmapImageBuilder(bitmap).build()
            val result = landmarker.detect(mpImage)

            if (result.landmarks().isNotEmpty()) {
                val landmarks = result.landmarks()[0]
                val points = landmarks.map { landmark ->
                    PosePoint(
                        x = landmark.x(),
                        y = landmark.y(),
                        confidence = landmark.visibility().orElse(0f)
                    )
                }
                _posePoints.value = points
            } else {
                _posePoints.value = emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in pose detection", e)
        }
    }

    override fun onCleared() {
        super.onCleared()
        retriever?.release()
        poseLandmarker?.close()
    }

    companion object {
        private const val TAG = "VideoPlaybackVM"
    }
}
