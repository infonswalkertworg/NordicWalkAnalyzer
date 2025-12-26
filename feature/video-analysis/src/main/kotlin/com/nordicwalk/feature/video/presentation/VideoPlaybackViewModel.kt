package com.nordicwalk.feature.video.presentation

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.provider.MediaStore
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
                videoPath = path
                val file = File(path)
                if (!file.exists()) {
                    _statusMessage.value = "檔案不存在"
                    return@launch
                }
                
                retriever = MediaMetadataRetriever()
                retriever?.setDataSource(path)
                
                val durationStr = retriever?.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )
                val duration = durationStr?.toLongOrNull() ?: 0L
                val frameRate = 30  // 預設 30fps
                _totalFrames.value = ((duration / 1000) * frameRate).toInt()
                
                // 載入第一幀
                loadFrame(0)
            } catch (e: Exception) {
                _statusMessage.value = "載入失敗: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun loadFrame(frameIndex: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (retriever == null) return@launch
                
                val timeUs = (frameIndex * 1000000L / 30).coerceAtMost(
                    ((_totalFrames.value - 1) * 1000000L / 30).toLong()
                )
                
                val bitmap = retriever?.getFrameAtTime(
                    timeUs,
                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                )
                
                if (bitmap != null) {
                    _frameBitmap.value = bitmap
                    _currentFrame.value = frameIndex
                    
                    // 提取姿態信息
                    val posePoints = poseAnalyzer.extractPosePoints(bitmap)
                    _posePoints.value = posePoints
                }
            } catch (e: Exception) {
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
                    _statusMessage.value = "截圖已保存: $filename"
                }
            } catch (e: Exception) {
                _statusMessage.value = "截圖失敗: ${e.message}"
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
