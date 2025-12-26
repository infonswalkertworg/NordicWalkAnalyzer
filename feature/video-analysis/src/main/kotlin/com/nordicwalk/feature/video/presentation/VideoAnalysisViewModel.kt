package com.nordicwalk.feature.video.presentation

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.feature.video.domain.model.AnalysisSummary
import com.nordicwalk.feature.video.domain.model.PoseAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class VideoAnalysisViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {
    // 從 SavedStateHandle 獲取編碼的路徑，然後解碼
    private val videoPath: String? = savedStateHandle.get<String>("encodedVideoPath")?.let { Uri.decode(it) }
    private val poseAnalyzer = PoseAnalyzer(context)
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()
    
    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()
    
    private val _analysisResults = MutableStateFlow<List<PoseAnalysisResult>>(emptyList())
    val analysisResults: StateFlow<List<PoseAnalysisResult>> = _analysisResults.asStateFlow()
    
    private val _analysisSummary = MutableStateFlow<AnalysisSummary?>(null)
    val analysisSummary: StateFlow<AnalysisSummary?> = _analysisSummary.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _videoDuration = MutableStateFlow(0L)
    val videoDuration: StateFlow<Long> = _videoDuration.asStateFlow()
    
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()
    
    private var currentVideoPath = videoPath

    init {
        if (!currentVideoPath.isNullOrEmpty()) {
            validateAndLoadVideo(currentVideoPath!!)
        } else {
            _errorMessage.value = "未找到視頻路徑"
        }
    }

    private fun validateAndLoadVideo(path: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val file = File(path)
                if (!file.exists()) {
                    _errorMessage.value = "視頻檔案不存在: ${file.name}"
                    _statusMessage.value = "錯誤: 檔案不存在"
                    return@launch
                }
                
                if (file.length() == 0L) {
                    _errorMessage.value = "視頻檔案為空"
                    _statusMessage.value = "錯誤: 檔案為空"
                    return@launch
                }
                
                loadVideoInfo()
            } catch (e: Exception) {
                _errorMessage.value = "檢查檔案失敗: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun loadVideo(path: String) {
        currentVideoPath = path
        validateAndLoadVideo(path)
    }

    private fun loadVideoInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (currentVideoPath == null) {
                    _errorMessage.value = "視頻路徑為空"
                    return@launch
                }
                
                _statusMessage.value = "正在載入視頻..."
                
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(currentVideoPath)
                
                val durationStr = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )
                _videoDuration.value = durationStr?.toLongOrNull() ?: 0L
                
                val widthStr = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                )
                val heightStr = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
                )
                
                retriever.release()
                
                if (_videoDuration.value > 0L) {
                    _statusMessage.value = "已載入視頻 (${_videoDuration.value / 1000}s, ${widthStr}x${heightStr})"
                    _errorMessage.value = null // 清除錯誤
                } else {
                    _errorMessage.value = "無法讀取視頻時長"
                }
                
            } catch (e: Exception) {
                _errorMessage.value = "載入視頻失敗: ${e.message}"
                _statusMessage.value = "錯誤: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    fun analyzeVideo(framesPerSecond: Int = 10) {
        viewModelScope.launch(Dispatchers.Default) {
            if (currentVideoPath.isNullOrEmpty()) {
                _errorMessage.value = "沒有選擇視頻"
                return@launch
            }
            
            // 再次檢查檔案是否存在
            val file = File(currentVideoPath!!)
            if (!file.exists()) {
                _errorMessage.value = "分析失敗: ${file.name} 不存在"
                return@launch
            }
            
            try {
                _isAnalyzing.value = true
                _statusMessage.value = "分析中..."
                _analysisResults.value = emptyList()
                _errorMessage.value = null
                _analysisProgress.value = 0f
                
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(currentVideoPath)
                
                val duration = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLongOrNull() ?: 0L
                
                if (duration == 0L) {
                    _isAnalyzing.value = false
                    _errorMessage.value = "無法讀取視頻時長"
                    retriever.release()
                    return@launch
                }
                
                val frameInterval = 1000000L / framesPerSecond
                val results = mutableListOf<PoseAnalysisResult>()
                
                var currentTime = 0L
                var frameIndex = 0
                val totalFramesToAnalyze = (duration * 1000 / frameInterval).toInt()
                
                while (currentTime <= duration * 1000) {
                    val bitmap = retriever.getFrameAtTime(
                        currentTime,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    
                    if (bitmap != null) {
                        val result = poseAnalyzer.analyzePose(
                            bitmap,
                            frameIndex = frameIndex,
                            trainingRecordId = 0L
                        )
                        results.add(result)
                        bitmap.recycle()
                        
                        _currentFrameIndex.value = frameIndex
                        _analysisProgress.value = (frameIndex.toFloat() / totalFramesToAnalyze).coerceIn(0f, 1f)
                        frameIndex++
                    }
                    
                    currentTime += frameInterval
                }
                
                retriever.release()
                
                _analysisResults.value = results
                _analysisSummary.value = generateSummary(results)
                _isAnalyzing.value = false
                _statusMessage.value = "分析完成！分析了 ${results.size} 幀"
                
            } catch (e: Exception) {
                _isAnalyzing.value = false
                _errorMessage.value = "分析失敗: ${e.message}"
                _statusMessage.value = "錯誤: ${e.message}"
                e.printStackTrace()
            }
        }
    }

    private fun generateSummary(results: List<PoseAnalysisResult>): AnalysisSummary {
        if (results.isEmpty()) {
            return AnalysisSummary(
                overallScore = 0f,
                commonIssues = emptyList(),
                keyRecommendations = emptyList(),
                totalFramesAnalyzed = 0
            )
        }
        
        val avgPostureScore = results.map { it.postureScore }.average().toFloat()
        val avgBalanceScore = results.map { it.balanceScore }.average().toFloat()
        val avgArmSwingScore = results.map { it.armSwingScore }.average().toFloat()
        
        val overallScore = (avgPostureScore + avgBalanceScore + avgArmSwingScore) / 3
        
        val allIssues = results.flatMap { it.issues }
        val issueFrequency = allIssues.groupingBy { it }.eachCount()
        val commonIssues = issueFrequency
            .filter { it.value > results.size * 0.2f }
            .keys
            .toList()
        
        val allRecommendations = results.flatMap { it.recommendations }
        val uniqueRecommendations = allRecommendations.distinct().take(5)
        
        return AnalysisSummary(
            overallScore = overallScore,
            commonIssues = commonIssues,
            keyRecommendations = uniqueRecommendations,
            totalFramesAnalyzed = results.size
        )
    }

    fun resetError() {
        _errorMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        poseAnalyzer.close()
    }
}
