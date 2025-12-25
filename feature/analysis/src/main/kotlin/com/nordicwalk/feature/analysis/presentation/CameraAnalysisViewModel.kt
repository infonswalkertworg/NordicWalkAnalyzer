package com.nordicwalk.feature.analysis.presentation

import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.core.data.repository.StudentRepository
import com.nordicwalk.core.data.repository.AnalysisRepository
import com.nordicwalk.core.domain.model.AnalysisSession
import com.nordicwalk.core.domain.model.CaptureSource
import com.nordicwalk.core.domain.model.PoseDetectionResult
import com.nordicwalk.core.domain.model.PoseFrame
import com.nordicwalk.core.domain.model.PoseMetrics
import com.nordicwalk.core.domain.model.ViewDirection
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * ViewModel for real-time camera pose analysis
 * Manages camera state, pose detection, and session recording
 */
@HiltViewModel
class CameraAnalysisViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val studentId: Long = savedStateHandle["studentId"] ?: 0L

    // UI State
    private val _uiState = MutableStateFlow<CameraAnalysisUiState>(CameraAnalysisUiState.Initializing)
    val uiState: StateFlow<CameraAnalysisUiState> = _uiState.asStateFlow()

    // Session State
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _currentDirection = MutableStateFlow(ViewDirection.FRONT)
    val currentDirection: StateFlow<ViewDirection> = _currentDirection.asStateFlow()

    // Frame Data
    private val _currentFrame = MutableStateFlow<PoseFrame?>(null)
    val currentFrame: StateFlow<PoseFrame?> = _currentFrame.asStateFlow()

    private val _currentMetrics = MutableStateFlow<PoseMetrics?>(null)
    val currentMetrics: StateFlow<PoseMetrics?> = _currentMetrics.asStateFlow()

    private val _frameCount = MutableStateFlow(0)
    val frameCount: StateFlow<Int> = _frameCount.asStateFlow()

    private val _fps = MutableStateFlow(0f)
    val fps: StateFlow<Float> = _fps.asStateFlow()

    // Session Data
    private var currentSession: AnalysisSession? = null
    private val frames = mutableListOf<PoseFrame>()
    private val metrics = mutableListOf<PoseMetrics>()

    // Timing
    private var lastFrameTime = System.currentTimeMillis()
    private var frameTimeBuffer = mutableListOf<Long>()

    init {
        loadStudent()
    }

    /**
     * Load student information
     */
    private fun loadStudent() {
        viewModelScope.launch {
            _uiState.value = CameraAnalysisUiState.Loading
            try {
                val student = studentRepository.getStudent(studentId)
                if (student != null) {
                    _uiState.value = CameraAnalysisUiState.Ready
                } else {
                    _uiState.value = CameraAnalysisUiState.Error("Student not found")
                }
            } catch (e: Exception) {
                _uiState.value = CameraAnalysisUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Start recording a new analysis session
     */
    fun startRecording() {
        if (_isRecording.value) return

        frames.clear()
        metrics.clear()
        frameTimeBuffer.clear()
        _frameCount.value = 0

        currentSession = AnalysisSession(
            id = System.currentTimeMillis(),
            studentId = studentId,
            direction = _currentDirection.value,
            captureSource = CaptureSource.CAMERA,
            startTime = LocalDateTime.now(),
            frames = frames,
            metrics = metrics
        )

        _isRecording.value = true
    }

    /**
     * Stop recording and save session
     */
    fun stopRecording() {
        if (!_isRecording.value) return

        _isRecording.value = false

        currentSession?.let { session ->
            val updatedSession = session.copy(
                endTime = LocalDateTime.now(),
                frames = frames.toList(),
                metrics = metrics.toList()
            )

            viewModelScope.launch {
                try {
                    analysisRepository.saveAnalysisSession(updatedSession)
                    _uiState.value = CameraAnalysisUiState.SessionSaved(updatedSession.id)
                } catch (e: Exception) {
                    _uiState.value = CameraAnalysisUiState.Error("Failed to save session: ${e.message}")
                }
            }
        }
    }

    /**
     * Process a new frame from camera
     */
    fun processFrame(bitmap: Bitmap, detectionResult: PoseDetectionResult) {
        if (!_isRecording.value || detectionResult.frame == null) {
            return
        }

        // Update frame count
        _frameCount.value = _frameCount.value + 1

        // Store frame
        _currentFrame.value = detectionResult.frame
        frames.add(detectionResult.frame)

        // Update metrics if available
        detectionResult.frame?.let { frame ->
            val calculatedMetrics = calculateMetrics(frame)
            _currentMetrics.value = calculatedMetrics
            metrics.add(calculatedMetrics)
        }

        // Update FPS
        updateFps()
    }

    /**
     * Calculate pose metrics from frame
     */
    private fun calculateMetrics(frame: PoseFrame): PoseMetrics {
        // This is a placeholder - real implementation would calculate
        // all the biomechanical metrics from landmarks
        return PoseMetrics(
            frameId = frame.id,
            timestamp = frame.timestamp,
            overallConfidence = frame.landmarks.map { it.confidence }.average().toFloat()
        )
    }

    /**
     * Update FPS calculation
     */
    private fun updateFps() {
        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastFrameTime
        lastFrameTime = currentTime

        frameTimeBuffer.add(deltaTime)
        if (frameTimeBuffer.size > 30) {
            frameTimeBuffer.removeAt(0)
        }

        if (frameTimeBuffer.size >= 10) {
            val avgFrameTime = frameTimeBuffer.average()
            val calculatedFps = if (avgFrameTime > 0) 1000f / avgFrameTime.toFloat() else 0f
            _fps.value = calculatedFps
        }
    }

    /**
     * Change capture direction
     */
    fun setDirection(direction: ViewDirection) {
        _currentDirection.value = direction
    }

    /**
     * Pause/Resume recording
     */
    fun toggleRecording() {
        if (_isRecording.value) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    /**
     * Discard current session
     */
    fun discardSession() {
        stopRecording()
        frames.clear()
        metrics.clear()
        currentSession = null
        _currentFrame.value = null
        _currentMetrics.value = null
        _frameCount.value = 0
    }
}

/**
 * UI State for camera analysis
 */
sealed class CameraAnalysisUiState {
    object Initializing : CameraAnalysisUiState()
    object Loading : CameraAnalysisUiState()
    object Ready : CameraAnalysisUiState()
    data class SessionSaved(val sessionId: Long) : CameraAnalysisUiState()
    data class Error(val message: String) : CameraAnalysisUiState()
}
