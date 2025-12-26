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
            Log.d(TAG, "創建視頻檔案: ${currentVideoFile?.absolutePath}")
            
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
                                // 確認檔案存在
                                val file = File(path)
                                if (file.exists() && file.length() > 0) {
                                    Log.d(TAG, "錄影完成: $path (大小: ${file.length()} bytes)")
                                    callback.onRecordingStopped(path)
                                } else {
                                    Log.e(TAG, "檔案不存在或為空: $path")
                                    callback.onRecordingError("錄影檔案創建失敗")
                                }
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

            val path = currentVideoFile?.absolutePath
            Log.d(TAG, "停止錄影: $path")
            path
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
        // 使用 app-specific 外部儲存空間，不需要 WRITE_EXTERNAL_STORAGE 權限
        val videosDir = File(context.getExternalFilesDir(null), "videos")
        if (!videosDir.exists()) {
            val created = videosDir.mkdirs()
            Log.d(TAG, "創建目錄: ${videosDir.absolutePath}, 結果: $created")
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(videosDir, "NW_$timestamp.mp4")
        Log.d(TAG, "準備創建檔案: ${file.absolutePath}")
        return file
    }
}
