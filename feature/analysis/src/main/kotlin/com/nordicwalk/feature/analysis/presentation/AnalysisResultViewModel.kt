package com.nordicwalk.feature.analysis.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.core.data.repository.AnalysisRepository
import com.nordicwalk.core.domain.model.AnalysisSession
import com.nordicwalk.core.domain.model.PostureViolation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for displaying and analyzing results from a pose analysis session
 */
@HiltViewModel
class AnalysisResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val analysisRepository: AnalysisRepository
) : ViewModel() {

    private val sessionId: Long = savedStateHandle["sessionId"] ?: 0L

    // UI State
    private val _uiState = MutableStateFlow<AnalysisResultUiState>(AnalysisResultUiState.Loading)
    val uiState: StateFlow<AnalysisResultUiState> = _uiState.asStateFlow()

    // Session Data
    private val _session = MutableStateFlow<AnalysisSession?>(null)
    val session: StateFlow<AnalysisSession?> = _session.asStateFlow()

    // Violations
    private val _violations = MutableStateFlow<List<PostureViolation>>(emptyList())
    val violations: StateFlow<List<PostureViolation>> = _violations.asStateFlow()

    // Statistics
    private val _statistics = MutableStateFlow<AnalysisStatistics?>(null)
    val statistics: StateFlow<AnalysisStatistics?> = _statistics.asStateFlow()

    // Selected metric
    private val _selectedMetric = MutableStateFlow<MetricType?>(null)
    val selectedMetric: StateFlow<MetricType?> = _selectedMetric.asStateFlow()

    init {
        loadSession()
    }

    /**
     * Load analysis session and process results
     */
    private fun loadSession() {
        viewModelScope.launch {
            _uiState.value = AnalysisResultUiState.Loading
            try {
                val loadedSession = analysisRepository.getAnalysisSession(sessionId)
                if (loadedSession != null) {
                    _session.value = loadedSession
                    _violations.value = loadedSession.violations
                    _statistics.value = calculateStatistics(loadedSession)
                    _uiState.value = AnalysisResultUiState.Success
                } else {
                    _uiState.value = AnalysisResultUiState.Error("Session not found")
                }
            } catch (e: Exception) {
                _uiState.value = AnalysisResultUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Calculate statistics from session data
     */
    private fun calculateStatistics(session: AnalysisSession): AnalysisStatistics {
        val metrics = session.metrics
        
        return if (metrics.isNotEmpty()) {
            AnalysisStatistics(
                totalFrames = session.frames.size,
                totalDuration = calculateDuration(session),
                avgTrunkTilt = metrics.map { it.trunkTilt }.average().toFloat(),
                avgStepLength = metrics.map { it.stepLength }.average().toFloat(),
                avgStepWidth = metrics.map { it.stepWidth }.average().toFloat(),
                totalViolations = session.violations.size,
                criticalViolations = session.violations.count { it.severity.name == "CRITICAL" },
                avgConfidence = metrics.map { it.overallConfidence }.average().toFloat()
            )
        } else {
            AnalysisStatistics()
        }
    }

    /**
     * Calculate session duration in seconds
     */
    private fun calculateDuration(session: AnalysisSession): Int {
        return if (session.endTime != null) {
            java.time.temporal.ChronoUnit.SECONDS.between(
                session.startTime,
                session.endTime
            ).toInt()
        } else {
            0
        }
    }

    /**
     * Select a metric for detailed view
     */
    fun selectMetric(metric: MetricType) {
        _selectedMetric.value = metric
    }

    /**
     * Export results to JSON
     */
    fun exportAsJson(): String {
        _session.value?.let { session ->
            return buildJsonExport(session)
        }
        return ""
    }

    /**
     * Build JSON representation of results
     */
    private fun buildJsonExport(session: AnalysisSession): String {
        // Simplified JSON export - real implementation would use proper JSON serialization
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"sessionId\": ${session.id},\n")
        sb.append("  \"studentId\": ${session.studentId},\n")
        sb.append("  \"direction\": \"${session.direction}\",\n")
        sb.append("  \"frameCount\": ${session.frames.size},\n")
        sb.append("  \"violationCount\": ${session.violations.size},\n")
        sb.append("  \"startTime\": \"${session.startTime}\",\n")
        sb.append("  \"endTime\": \"${session.endTime}\"\n")
        sb.append("}")
        return sb.toString()
    }

    /**
     * Share analysis report
     */
    fun shareReport(): String {
        _session.value?.let { session ->
            _statistics.value?.let { stats ->
                return buildReportText(session, stats)
            }
        }
        return "No data available"
    }

    /**
     * Build human-readable report
     */
    private fun buildReportText(session: AnalysisSession, stats: AnalysisStatistics): String {
        return buildString {
            append("=== Nordic Walk Analysis Report ===\n\n")
            append("Session ID: ${session.id}\n")
            append("Duration: ${stats.totalDuration} seconds\n")
            append("Total Frames: ${stats.totalFrames}\n\n")
            
            append("=== Metrics ===\n")
            append("Avg Trunk Tilt: ${String.format("%.1f", stats.avgTrunkTilt)}°\n")
            append("Avg Step Length: ${String.format("%.2f", stats.avgStepLength)}m\n")
            append("Avg Step Width: ${String.format("%.2f", stats.avgStepWidth)}m\n")
            append("Avg Confidence: ${String.format("%.1f", stats.avgConfidence)}%\n\n")
            
            append("=== Issues Found ===\n")
            append("Total Violations: ${stats.totalViolations}\n")
            append("Critical Issues: ${stats.criticalViolations}\n\n")
            
            if (session.violations.isNotEmpty()) {
                append("=== Detailed Issues ===\n")
                session.violations.forEach { violation ->
                    append("- ${violation.description}\n")
                    append("  Suggestion: ${violation.suggestion}\n")
                }
            }
        }
    }
}

/**
 * Analysis statistics summary
 */
data class AnalysisStatistics(
    val totalFrames: Int = 0,
    val totalDuration: Int = 0,
    val avgTrunkTilt: Float = 0f,
    val avgStepLength: Float = 0f,
    val avgStepWidth: Float = 0f,
    val totalViolations: Int = 0,
    val criticalViolations: Int = 0,
    val avgConfidence: Float = 0f
)

/**
 * Metric types for detailed analysis
 */
enum class MetricType {
    POSTURE,
    ARM_SWING,
    STRIDE,
    PACE,
    VIOLATIONS
}

/**
 * UI State for analysis results
 */
sealed class AnalysisResultUiState {
    object Loading : AnalysisResultUiState()
    object Success : AnalysisResultUiState()
    data class Error(val message: String) : AnalysisResultUiState()
}
