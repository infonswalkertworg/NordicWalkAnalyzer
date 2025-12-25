package com.nordicwalk.feature.video.presentation

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.feature.video.domain.model.AnalysisSummary
import com.nordicwalk.feature.video.domain.model.PoseAnalysisResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VideoAnalysisViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val context: Context
) : ViewModel() {
    private val videoPath: String? = savedStateHandle["videoPath"]
    private val poseAnalyzer = PoseAnalyzer(context)
    
    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()
    
    private val _analysisProgress = MutableStateFlow(0f)  // 0-1
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()
    
    private val _currentFrameIndex = MutableStateFlow(0)
    val currentFrameIndex: StateFlow<Int> = _currentFrameIndex.asStateFlow()
    
    private val _analysisResults = MutableStateFlow<List<PoseAnalysisResult>>(emptyList())
    val analysisResults: StateFlow<List<PoseAnalysisResult>> = _analysisResults.asStateFlow()
    
    private val _analysisSummary = MutableStateFlow<AnalysisSummary?>(null)
    val analysisSummary: StateFlow<AnalysisSummary?> = _analysisSummary.asStateFlow()
    
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    
    private val _videoDuration = MutableStateFlow(0L)  // 毫秒合位
    val videoDuration: StateFlow<Long> = _videoDuration.asStateFlow()
    
    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()
    
    private var currentVideoPath = videoPath

    init {
        if (!currentVideoPath.isNullOrEmpty()) {
            loadVideoInfo()
        }
    }

    /**
     * 載入訓練記錄視频
     */
    fun loadVideo(path: String) {
        currentVideoPath = path
        loadVideoInfo()
    }

    /**
     * 載入訓練記錄診悠統計信息
     */
    private fun loadVideoInfo() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                if (currentVideoPath == null) return@launch
                
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(currentVideoPath)
                
                val durationStr = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )
                _videoDuration.value = durationStr?.toLongOrNull() ?: 0L
                
                retriever.release()
                _statusMessage.value = "訓練記錄已上載：時間${_videoDuration.value / 1000}s"
            } catch (e: Exception) {
                _errorMessage.value = "載入訓練記錄失敗: ${e.message}"
            }
        }
    }

    /**
     * 開始分析訓練記錄
     * 將訒診孺場推抽成幀數根據關節點分析
     */
    fun analyzeVideo(framesPerSecond: Int = 10) {
        viewModelScope.launch(Dispatchers.Default) {
            if (currentVideoPath == null) {
                _errorMessage.value = "沒有選擇訓練記錄"
                return@launch
            }
            
            try {
                _isAnalyzing.value = true
                _statusMessage.value = "不可變分析中..."
                _analysisResults.value = emptyList()
                
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(currentVideoPath)
                
                val duration = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_DURATION
                )?.toLongOrNull() ?: 0L
                
                val width = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH
                )?.toIntOrNull() ?: 1280
                
                val height = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT
                )?.toIntOrNull() ?: 720
                
                val frameInterval = 1000000L / framesPerSecond  // 微秒位
                val results = mutableListOf<PoseAnalysisResult>()
                
                var currentTime = 0L
                var frameIndex = 0
                val totalFramesToAnalyze = (duration / frameInterval).toInt()
                
                while (currentTime <= duration * 1000) {
                    val bitmap = retriever.getFrameAtTime(
                        currentTime,
                        MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                    )
                    
                    if (bitmap != null) {
                        val result = poseAnalyzer.analyzePose(
                            bitmap,
                            frameIndex = frameIndex,
                            trainingRecordId = 0L  // TODO: 正確的訓練記錄 ID
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
                _statusMessage.value = "分析完成！分析了 ${results.size} 幀
                
            } catch (e: Exception) {
                _isAnalyzing.value = false
                _errorMessage.value = "分析失敗: ${e.message}"
                _statusMessage.value = "錯誤: ${e.message}"
            }
        }
    }

    /**
     * 產生訓練記錄分析總結
     */
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
        
        // 敢護遽見了的問題
        val allIssues = results.flatMap { it.issues }
        val issueFrequency = allIssues.groupingBy { it }.eachCount()
        val commonIssues = issueFrequency
            .filter { it.value > results.size * 0.2f }  // 出現在 20% 以上的紀影
            .keys
            .toList()
        
        // 集合所有的建議
        val allRecommendations = results.flatMap { it.recommendations }
        val uniqueRecommendations = allRecommendations.distinct().take(5)  // 最多 5 個建議
        
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
