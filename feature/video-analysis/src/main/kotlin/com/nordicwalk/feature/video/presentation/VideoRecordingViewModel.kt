package com.nordicwalk.feature.video.presentation

import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.feature.video.util.RecordingCallback
import com.nordicwalk.feature.video.util.VideoRecorder
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
    private val videoRecorder = VideoRecorder(context)
    
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
    
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private var recordingStartTime = 0L

    /**
     * 初始化 VideoCapture
     */
    fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    ) {
        viewModelScope.launch {
            videoRecorder.initializeVideoCapture(
                lifecycleOwner = lifecycleOwner,
                cameraSelector = cameraSelector,
                onInitialized = {
                    _isInitialized.value = true
                    _statusMessage.value = "相機就緒"
                }
            )
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            if (!_isInitialized.value) {
                _errorMessage.value = "相機未初始化"
                return@launch
            }

            val success = videoRecorder.startRecording(
                callback = object : RecordingCallback {
                    override fun onRecordingStarted(filePath: String) {
                        _isRecording.value = true
                        _statusMessage.value = "錄製中..."
                        recordingStartTime = System.currentTimeMillis()
                        _recordingDuration.value = 0L
                    }

                    override fun onRecordingStopped(filePath: String) {
                        _isRecording.value = false
                        _recordedVideoPath.value = filePath
                        _statusMessage.value = "錄製完成"
                    }

                    override fun onRecordingCancelled() {
                        _isRecording.value = false
                        _statusMessage.value = "錄製已取消"
                        _recordingDuration.value = 0L
                    }

                    override fun onRecordingError(errorMessage: String) {
                        _isRecording.value = false
                        _errorMessage.value = errorMessage
                        _statusMessage.value = "錯誤: $errorMessage"
                    }
                }
            )
            
            if (!success) {
                _errorMessage.value = "錄製開始失敗"
                _statusMessage.value = "錯誤: 錄裭開始失敗"
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            val videoPath = videoRecorder.stopRecording()
            if (videoPath != null) {
                _recordedVideoPath.value = videoPath
                _statusMessage.value = "錄裭已保存: $videoPath"
            } else {
                _errorMessage.value = "停止錄裭失敗"
                _statusMessage.value = "錯誤: 停止錄裭失敗"
            }
        }
    }

    fun cancelRecording() {
        viewModelScope.launch {
            videoRecorder.cancelRecording()
            _recordedVideoPath.value = null
            _recordingDuration.value = 0L
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
            videoRecorder.cancelRecording()
        }
        videoRecorder.release()
    }
}
