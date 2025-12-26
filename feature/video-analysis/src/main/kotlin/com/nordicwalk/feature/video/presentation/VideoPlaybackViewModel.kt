package com.nordicwalk.feature.video.presentation

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.feature.video.util.PoseAnalyzerUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class PosePoint(
    val x: Float,
    val y: Float,
    val confidence: Float,
    val label: String
)

@HiltViewModel
class VideoPlaybackViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val TAG = "VideoPlaybackViewModel"
    private val poseAnalyzer = PoseAnalyzerUtil(context)
    private var retriever: MediaMetadataRetriever? = null
    private var videoPath: String? = null
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentFrame = MutableStateFlow(0)
    val currentFrame: StateFlow<Int> = _currentFrame.asStateFlow()
    
    private val _totalFrames = MutableStateFlow(0)
    val totalFrames: StateFlow<Int> = _totalFrames.asStateFlow()
    
    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()
    
    private val _frameBitmap = MutableStateFlow<Bitmap?>(null)
    val frameBitmap: StateFlow<Bitmap?> = _frameBitmap.asStateFlow()
    
    private val _posePoints = MutableStateFlow<List<PosePoint>>(emptyList())
    val posePoints: StateFlow<List<PosePoint>> = _posePoints.asStateFlow()
    
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    fun loadVideo(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                Log.d(TAG, "Loading video from path: $path")
                videoPath = path
                
                // 清理舊的 retriever
                retriever?.release()
                retriever = MediaMetadataRetriever()
                
                // 嘗試設置數據源
                try {
                    // 如果是 content:// URI
                    if (path.startsWith("content://")) {
                        retriever?.setDataSource(context, Uri.parse(path))
                    } else {
                        // 如果是文件路徑
                        val file = File(path)
                        if (!file.exists()) {
                            withContext(Dispatchers.Main) {
                                _statusMessage.value = "檔案不存在: $path"
                            }
                            Log.e(TAG, "File does not exist: $path")
                            return@launch
                        }
                        retriever?.setDataSource(path)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error setting data source", e)
                    withContext(Dispatchers.Main) {
                        _statusMessage.value = "無法載入視頻: ${e.message}"
                    }
                    return@launch
                }
                
                // 獲取視頻時長和幀數
                val durationStr = retriever?.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )
                val duration = durationStr?.toLongOrNull() ?: 0L
                Log.d(TAG, "Video duration: $duration ms")
                
                if (duration <= 0) {
                    withContext(Dispatchers.Main) {
                        _statusMessage.value = "無效的視頻文件"
                    }
                    return@launch
                }
                
                val frameRate = 30  // 預設 30fps
                val frames = ((duration / 1000.0) * frameRate).toInt()
                
                withContext(Dispatchers.Main) {
                    _totalFrames.value = frames
                    _currentFrame.value = 0
                    Log.d(TAG, "Total frames: $frames")
                }
                
                // 載入第一幀
                loadFrame(0)
                
                withContext(Dispatchers.Main) {
                    _statusMessage.value = "視頻已載入"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading video", e)
                withContext(Dispatchers.Main) {
                    _statusMessage.value = "載入失敗: ${e.message}"
                }
                e.printStackTrace()
            }
        }
    }

    private fun loadFrame(frameIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (retriever == null) {
                    Log.e(TAG, "Retriever is null")
                    return@launch
                }
                
                val timeUs = (frameIndex * 1000000L / 30)
                Log.d(TAG, "Loading frame $frameIndex at time $timeUs us")
                
                val bitmap = retriever?.getFrameAtTime(
                    timeUs,
                    MediaMetadataRetriever.OPTION_CLOSEST
                )
                
                if (bitmap != null) {
                    Log.d(TAG, "Frame loaded successfully: ${bitmap.width}x${bitmap.height}")
                    
                    withContext(Dispatchers.Main) {
                        // 回收舊的 bitmap
                        _frameBitmap.value?.recycle()
                        _frameBitmap.value = bitmap
                        _currentFrame.value = frameIndex
                    }
                    
                    // 提取姿態信息（在背景執行）
                    try {
                        val posePoints = poseAnalyzer.extractPosePoints(bitmap)
                        withContext(Dispatchers.Main) {
                            _posePoints.value = posePoints
                        }
                        Log.d(TAG, "Extracted ${posePoints.size} pose points")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error extracting pose points", e)
                    }
                } else {
                    Log.e(TAG, "Failed to extract frame at index $frameIndex")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading frame", e)
                e.printStackTrace()
            }
        }
    }

    fun nextFrame() {
        val next = (_currentFrame.value + 1).coerceAtMost(_totalFrames.value - 1)
        if (next >= _totalFrames.value - 1) {
            _isPlaying.value = false
        }
        loadFrame(next)
    }

    fun previousFrame() {
        val prev = (_currentFrame.value - 1).coerceAtLeast(0)
        loadFrame(prev)
    }

    fun seekFrame(frameIndex: Int) {
        val bounded = frameIndex.coerceIn(0, _totalFrames.value - 1)
        _isPlaying.value = false
        loadFrame(bounded)
    }

    fun togglePlayPause() {
        _isPlaying.value = !_isPlaying.value
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
    }

    fun captureFrame(context: Context, bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val timestamp = SimpleDateFormat(
                    "yyyy-MM-dd_HH-mm-ss",
                    Locale.getDefault()
                ).format(Date())
                val filename = "NWA_${timestamp}.jpg"
                
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/NordicWalkAnalyzer"
                    )
                }
                
                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )
                
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
                    }
                    withContext(Dispatchers.Main) {
                        _statusMessage.value = "截圖已保存: $filename"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _statusMessage.value = "截圖失敗: ${e.message}"
                }
                e.printStackTrace()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        retriever?.release()
        poseAnalyzer.close()
        _frameBitmap.value?.recycle()
    }
}
