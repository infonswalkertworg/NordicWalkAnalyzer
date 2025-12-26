package com.nordicwalk.feature.video.util

import android.content.Context
import android.util.Log
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 簡化的視頻錄製器
 */
class VideoRecorder(private val context: Context) {
    private var recording: Recording? = null
    private var currentVideoFile: File? = null
    private var recordingCallback: RecordingCallback? = null
    var isRecording = false
        private set

    companion object {
        private const val TAG = "VideoRecorder"
    }

    /**
     * 開始錄製視頻
     */
    fun startRecording(
        videoCapture: VideoCapture<Recorder>,
        callback: RecordingCallback? = null
    ): Boolean {
        return try {
            if (isRecording) {
                callback?.onRecordingError("已在錄裭中")
                return false
            }

            recordingCallback = callback
            currentVideoFile = createVideoFile()

            val fileOutputOptions = FileOutputOptions.Builder(currentVideoFile!!).build()

            recording = videoCapture.output
                .prepareRecording(context, fileOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { videoRecordEvent ->
                    when (videoRecordEvent) {
                        is VideoRecordEvent.Start -> {
                            isRecording = true
                            callback?.onRecordingStarted(currentVideoFile?.absolutePath ?: "")
                            Log.d(TAG, "錄裭開始: ${currentVideoFile?.name}")
                        }
                        is VideoRecordEvent.Finalize -> {
                            isRecording = false
                            if (!videoRecordEvent.hasError()) {
                                val videoPath = currentVideoFile?.absolutePath ?: ""
                                Log.d(TAG, "錄裭完成: $videoPath")
                                callback?.onRecordingStopped(videoPath)
                            } else {
                                val error = "錄裭錯誤: ${videoRecordEvent.error}"
                                Log.e(TAG, error)
                                callback?.onRecordingError(error)
                            }
                        }
                        is VideoRecordEvent.Status -> {
                            Log.d(TAG, "錄裭中... 時間: ${videoRecordEvent.recordingStats.recordedDurationNanos / 1_000_000_000}s")
                        }
                    }
                }

            true
        } catch (e: Exception) {
            Log.e(TAG, "錄裭失敗", e)
            recordingCallback?.onRecordingError("錄裭失敗: ${e.message}")
            false
        }
    }

    /**
     * 停止錄裭
     */
    fun stopRecording(): String? {
        return try {
            if (!isRecording || recording == null) {
                Log.w(TAG, "未進行錄裭")
                return null
            }

            recording?.stop()
            recording = null

            val videoPath = currentVideoFile?.absolutePath
            Log.d(TAG, "錄裭停止: $videoPath")
            videoPath
        } catch (e: Exception) {
            Log.e(TAG, "停止錄裭失敗", e)
            recordingCallback?.onRecordingError("停止錄裭失敗: ${e.message}")
            null
        }
    }

    /**
     * 取消錄裭
     */
    fun cancelRecording() {
        try {
            recording?.stop()
            recording = null
            isRecording = false

            currentVideoFile?.delete()
            currentVideoFile = null

            recordingCallback?.onRecordingCancelled()
            Log.d(TAG, "錄裭已取消")
        } catch (e: Exception) {
            Log.e(TAG, "取消錄裭失敗", e)
        }
    }

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
}

interface RecordingCallback {
    fun onRecordingStarted(filePath: String)
    fun onRecordingStopped(filePath: String)
    fun onRecordingCancelled()
    fun onRecordingError(errorMessage: String)
}
