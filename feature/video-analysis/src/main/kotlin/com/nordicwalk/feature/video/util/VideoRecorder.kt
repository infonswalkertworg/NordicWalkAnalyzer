package com.nordicwalk.feature.video.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 視频錄製器
 */
class VideoRecorder(private val context: Context) {
    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var currentVideoFile: File? = null
    private var recordingCallback: RecordingCallback? = null

    companion object {
        private const val TAG = "VideoRecorder"
        private const val VIDEO_BITRATE = 5000000 // 5 Mbps
        private const val AUDIO_BITRATE = 128000 // 128 kbps
        private const val VIDEO_FRAME_RATE = 30
        private const val VIDEO_WIDTH = 1280
        private const val VIDEO_HEIGHT = 720
    }

    /**
     * 開始錄製視频
     */
    fun startRecording(
        cameraId: Int = 0,
        callback: RecordingCallback? = null
    ): Boolean {
        return try {
            recordingCallback = callback
            currentVideoFile = createVideoFile()
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.CAMERA)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(AUDIO_BITRATE)
                setVideoEncodingBitRate(VIDEO_BITRATE)
                setVideoFrameRate(VIDEO_FRAME_RATE)
                setVideoSize(VIDEO_WIDTH, VIDEO_HEIGHT)
                setOrientationHint(90)
                setOutputFile(currentVideoFile?.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            callback?.onRecordingStarted(currentVideoFile?.absolutePath ?: "")
            Log.d(TAG, "錄製開始: ${currentVideoFile?.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "錄製失敗", e)
            recordingCallback?.onRecordingError("錄製失敗: ${e.message}")
            mediaRecorder?.release()
            mediaRecorder = null
            false
        }
    }

    /**
     * 停止錄製
     */
    fun stopRecording(): String? {
        return try {
            if (!isRecording || mediaRecorder == null) {
                Log.w(TAG, "未進行錄製")
                return null
            }

            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            val videoPath = currentVideoFile?.absolutePath
            Log.d(TAG, "錄製結束: $videoPath")
            recordingCallback?.onRecordingStopped(videoPath ?: "")
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
            if (isRecording) {
                mediaRecorder?.apply {
                    try {
                        stop()
                    } catch (e: Exception) {
                        Log.w(TAG, "停止錄製時的錯誤", e)
                    }
                    release()
                }
            }
            
            currentVideoFile?.delete()
            currentVideoFile = null
            mediaRecorder = null
            isRecording = false
            
            recordingCallback?.onRecordingCancelled()
            Log.d(TAG, "錄製已取消")
        } catch (e: Exception) {
            Log.e(TAG, "取消錄製失敗", e)
        }
    }

    fun getIsRecording(): Boolean = isRecording

    fun getCurrentVideoFile(): File? = currentVideoFile

    private fun createVideoFile(): File {
        val videosDir = File(context.cacheDir, "videos")
        if (!videosDir.exists()) {
            videosDir.mkdirs()
        }

        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return File(videosDir, "NW_$timestamp.mp4")
    }

    fun clearAllVideos() {
        try {
            val videosDir = File(context.cacheDir, "videos")
            if (videosDir.exists()) {
                videosDir.deleteRecursively()
            }
            Log.d(TAG, "已清除所有視频")
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
