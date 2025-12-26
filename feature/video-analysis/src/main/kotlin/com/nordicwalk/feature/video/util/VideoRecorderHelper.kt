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
 * 簡化的錄影工具 - 直接使用 VideoCapture
 */
class VideoRecorderHelper(private val context: Context) {
    private var recording: Recording? = null
    private var isRecording = false
    private var currentVideoFile: File? = null

    companion object {
        private const val TAG = "VideoRecorderHelper"
    }

    /**
     * 開始錄影
     */
    fun startRecording(
        videoCapture: VideoCapture<Recorder>,
        callback: RecordingCallback
    ): Boolean {
        return try {
            if (isRecording) {
                callback.onRecordingError("已在錄影中")
                return false
            }

            currentVideoFile = createVideoFile()
            val fileOutputOptions = FileOutputOptions.Builder(currentVideoFile!!).build()

            recording = videoCapture.output
                .prepareRecording(context, fileOutputOptions)
                .withAudioEnabled()
                .start(ContextCompat.getMainExecutor(context)) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> {
                            isRecording = true
                            callback.onRecordingStarted(currentVideoFile?.absolutePath ?: "")
                            Log.d(TAG, "錄影開始: ${currentVideoFile?.name}")
                        }
                        is VideoRecordEvent.Finalize -> {
                            isRecording = false
                            if (!event.hasError()) {
                                val path = currentVideoFile?.absolutePath ?: ""
                                Log.d(TAG, "錄影完成: $path")
                                callback.onRecordingStopped(path)
                            } else {
                                val error = "錄影錯誤: ${event.cause?.message ?: event.error}"
                                Log.e(TAG, error)
                                callback.onRecordingError(error)
                            }
                        }
                        is VideoRecordEvent.Status -> {
                            Log.d(TAG, "錄影中 - 大小: ${event.recordingStats.numBytesRecorded}")
                        }
                    }
                }

            true
        } catch (e: Exception) {
            Log.e(TAG, "錄影失敗", e)
            callback.onRecordingError("錄影失敗: ${e.message}")
            false
        }
    }

    /**
     * 停止錄影
     */
    fun stopRecording(): String? {
        return try {
            if (!isRecording || recording == null) {
                Log.w(TAG, "未進行錄影")
                return null
            }

            recording?.stop()
            recording = null

            currentVideoFile?.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "停止錄影失敗", e)
            null
        }
    }

    /**
     * 取消錄影
     */
    fun cancelRecording() {
        try {
            recording?.stop()
            recording = null
            isRecording = false

            currentVideoFile?.delete()
            currentVideoFile = null

            Log.d(TAG, "錄影已取消")
        } catch (e: Exception) {
            Log.e(TAG, "取消錄影失敗", e)
        }
    }

    fun isRecording(): Boolean = isRecording

    private fun createVideoFile(): File {
        val videosDir = File(context.getExternalFilesDir(null), "videos")
        if (!videosDir.exists()) {
            videosDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(videosDir, "NW_$timestamp.mp4")
    }
}
