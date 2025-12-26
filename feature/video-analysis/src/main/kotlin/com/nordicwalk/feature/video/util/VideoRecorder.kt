package com.nordicwalk.feature.video.util

import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.Executor

/**
 * 使用 CameraX VideoCapture 的視頻錄製器
 */
class VideoRecorder(private val context: Context) {
    private var recording: Recording? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var isRecording = false
    private var currentVideoFile: File? = null
    private var recordingCallback: RecordingCallback? = null
    private var cameraProvider: ProcessCameraProvider? = null

    companion object {
        private const val TAG = "VideoRecorder"
    }

    /**
     * 初始化 VideoCapture
     */
    fun initializeVideoCapture(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
        onInitialized: () -> Unit = {}
    ) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val executor: Executor = ContextCompat.getMainExecutor(context)

        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()

                // 配置 Recorder
                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HD,
                            FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)
                        )
                    )
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                // 綁定到生命週期
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    videoCapture
                )

                Log.d(TAG, "VideoCapture 初始化成功")
                onInitialized()
            } catch (e: Exception) {
                Log.e(TAG, "VideoCapture 初始化失敗", e)
                recordingCallback?.onRecordingError("初始化失敗: ${e.message}")
            }
        }, executor)
    }

    /**
     * 開始錄製視頻
     */
    fun startRecording(
        callback: RecordingCallback? = null
    ): Boolean {
        return try {
            if (videoCapture == null) {
                callback?.onRecordingError("VideoCapture 未初始化")
                return false
            }

            if (isRecording) {
                callback?.onRecordingError("已在錄製中")
                return false
            }

            recordingCallback = callback
            currentVideoFile = createVideoFile()

            val fileOutputOptions = FileOutputOptions.Builder(currentVideoFile!!).build()

            recording = videoCapture!!.output
                .prepareRecording(context, fileOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { videoRecordEvent ->
                    when (videoRecordEvent) {
                        is VideoRecordEvent.Start -> {
                            isRecording = true
                            callback?.onRecordingStarted(currentVideoFile?.absolutePath ?: "")
                            Log.d(TAG, "錄製開始: ${currentVideoFile?.name}")
                        }
                        is VideoRecordEvent.Finalize -> {
                            isRecording = false
                            if (!videoRecordEvent.hasError()) {
                                val videoPath = currentVideoFile?.absolutePath ?: ""
                                Log.d(TAG, "錄製完成: $videoPath")
                                callback?.onRecordingStopped(videoPath)
                            } else {
                                val error = "錄製錯誤: ${videoRecordEvent.error}"
                                Log.e(TAG, error)
                                callback?.onRecordingError(error)
                            }
                        }
                        is VideoRecordEvent.Status -> {
                            // 可以在這裡更新錄製狀態
                            Log.d(TAG, "錄製中...")
                        }
                        is VideoRecordEvent.Pause -> {
                            Log.d(TAG, "錄製暫停")
                        }
                        is VideoRecordEvent.Resume -> {
                            Log.d(TAG, "錄製恢復")
                        }
                    }
                }

            true
        } catch (e: Exception) {
            Log.e(TAG, "錄製失敗", e)
            recordingCallback?.onRecordingError("錄製失敗: ${e.message}")
            false
        }
    }

    /**
     * 停止錄製
     */
    fun stopRecording(): String? {
        return try {
            if (!isRecording || recording == null) {
                Log.w(TAG, "未進行錄製")
                return null
            }

            recording?.stop()
            recording = null

            val videoPath = currentVideoFile?.absolutePath
            Log.d(TAG, "錄製停止: $videoPath")
            videoPath
        } catch (e: Exception) {
            Log.e(TAG, "停止錄製失敗", e)
            recordingCallback?.onRecordingError("停止錄製失敗: ${e.message}")
            null
        }
    }

    /**
     * 取消錄製
     */
    fun cancelRecording() {
        try {
            recording?.stop()
            recording = null
            isRecording = false

            currentVideoFile?.delete()
            currentVideoFile = null

            recordingCallback?.onRecordingCancelled()
            Log.d(TAG, "錄製已取消")
        } catch (e: Exception) {
            Log.e(TAG, "取消錄製失敗", e)
        }
    }

    fun getIsRecording(): Boolean = isRecording

    fun getCurrentVideoFile(): File? = currentVideoFile

    private fun createVideoFile(): File {
        val videosDir = File(context.getExternalFilesDir(null), "videos")
        if (!videosDir.exists()) {
            videosDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(videosDir, "NW_$timestamp.mp4")
    }

    fun clearAllVideos() {
        try {
            val videosDir = File(context.getExternalFilesDir(null), "videos")
            if (videosDir.exists()) {
                videosDir.deleteRecursively()
            }
            Log.d(TAG, "已清除所有視頻")
        } catch (e: Exception) {
            Log.e(TAG, "清理檔案失敗", e)
        }
    }

    fun release() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        videoCapture = null
    }
}

interface RecordingCallback {
    fun onRecordingStarted(filePath: String)
    fun onRecordingStopped(filePath: String)
    fun onRecordingCancelled()
    fun onRecordingError(errorMessage: String)
}
