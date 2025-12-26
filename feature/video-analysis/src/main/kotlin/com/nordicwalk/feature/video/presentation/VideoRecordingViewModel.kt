package com.nordicwalk.feature.video.presentation

import android.content.Context
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.feature.video.util.RecordingCallback
import com.nordicwalk.feature.video.util.VideoRecorderHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoRecordingViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val recorderHelper = VideoRecorderHelper(context)
    private var videoCapture: VideoCapture<Recorder>? = null
    
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    
    private val _recordingDuration = MutableStateFlow(0L)
    val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()
    
    private val _recordedVideoPath = MutableStateFlow<String?>(null)
    val recordedVideoPath: StateFlow<String?> = _recordedVideoPath.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()
    
    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()
    
    private var recordingStartTime = 0L

    /**
     * 設定 VideoCapture
     */
    fun setVideoCapture(capture: VideoCapture<Recorder>) {
        videoCapture = capture
        _isCameraReady.value = true
        _statusMessage.value = "相機就緒"
    }

    fun startRecording() {
        viewModelScope.launch {
            if (videoCapture == null) {
                _errorMessage.value = "相機未就緒"
                return@launch
            }

            val success = recorderHelper.startRecording(
                videoCapture = videoCapture!!,
                callback = object : RecordingCallback {
                    override fun onRecordingStarted(filePath: String) {
                        _isRecording.value = true
                        _statusMessage.value = "錄影中..."
                        _errorMessage.value = null  // 清除錯誤
                        recordingStartTime = System.currentTimeMillis()
                        _recordingDuration.value = 0L
                    }

                    override fun onRecordingStopped(filePath: String) {
                        _isRecording.value = false
                        _recordedVideoPath.value = filePath
                        _statusMessage.value = "錄影完成"
                        _errorMessage.value = null  // 清除錯誤
                    }

                    override fun onRecordingCancelled() {
                        _isRecording.value = false
                        _statusMessage.value = "已取消錄影"  // 顯示取消訊息
                        _errorMessage.value = null  // 清除錯誤
                        _recordingDuration.value = 0L
                        _recordedVideoPath.value = null
                    }

                    override fun onRecordingError(errorMessage: String) {
                        _isRecording.value = false
                        _errorMessage.value = errorMessage
                        _statusMessage.value = "錯誤: $errorMessage"
                    }
                }
            )
            
            if (!success) {
                _errorMessage.value = "錄影開始失敗"
                _statusMessage.value = "錯誤: 錄影開始失敗"
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            val videoPath = recorderHelper.stopRecording()
            if (videoPath != null) {
                _recordedVideoPath.value = videoPath
                _statusMessage.value = "錄影已保存"
                _errorMessage.value = null  // 清除錯誤
            } else {
                _errorMessage.value = "停止錄影失敗"
                _statusMessage.value = "錯誤: 停止錄影失敗"
            }
        }
    }

    fun cancelRecording() {
        viewModelScope.launch {
            recorderHelper.cancelRecording()
            // cancelRecording 會觸發 onRecordingCancelled 回調
            // 但也在這裡確保狀態正確
            _recordedVideoPath.value = null
            _recordingDuration.value = 0L
            _errorMessage.value = null  // 清除錯誤
        }
    }

    fun updateRecordingDuration() {
        if (_isRecording.value && recordingStartTime > 0L) {
            _recordingDuration.value = System.currentTimeMillis() - recordingStartTime
        }
    }

    fun resetError() {
        _errorMessage.value = null
    }

    fun resetStatus() {
        _statusMessage.value = ""
    }

    override fun onCleared() {
        super.onCleared()
        if (_isRecording.value) {
            recorderHelper.cancelRecording()
        }
    }
}
