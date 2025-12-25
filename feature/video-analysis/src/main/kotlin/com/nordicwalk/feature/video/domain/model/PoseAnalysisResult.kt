package com.nordicwalk.feature.video.domain.model

import java.util.Date

/**
 * 姿勢分析結果
 * 包含檢測到的身體關節點和動作評估
 */
data class PoseAnalysisResult(
    val id: Long = 0,
    val trainingRecordId: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    
    // 關節點信息 (x, y, confidence)
    val landmarks: List<Landmark> = emptyList(),
    
    // 動作評估分數 (0-100)
    val postureScore: Float = 0f,
    val balanceScore: Float = 0f,
    val armSwingScore: Float = 0f,
    
    // 檢測到的問題
    val issues: List<PostureIssue> = emptyList(),
    
    // 建議
    val recommendations: List<String> = emptyList(),
    
    // 幀索引
    val frameIndex: Int = 0
) {
    companion object {
        fun empty(trainingRecordId: Long = 0): PoseAnalysisResult {
            return PoseAnalysisResult(trainingRecordId = trainingRecordId)
        }
    }
}

/**
 * 關節點
 */
data class Landmark(
    val name: String,           // 關節名稱 (e.g., "left_shoulder")
    val x: Float,               // 歸一化 X 座標 (0-1)
    val y: Float,               // 歸一化 Y 座標 (0-1)
    val z: Float = 0f,          // 歸一化 Z 座標 (深度)
    val visibility: Float = 1f  // 可見度 (0-1)
) {
    fun toScreenCoordinates(width: Int, height: Int): Pair<Int, Int> {
        return Pair((x * width).toInt(), (y * height).toInt())
    }
}

/**
 * 姿勢問題類型
 */
enum class PostureIssue(val description: String, val severity: IssueSeverity) {
    // 上身相關
    FORWARD_HEAD("頭部過度前傾", IssueSeverity.HIGH),
    ROUNDED_SHOULDERS("圓肩", IssueSeverity.MEDIUM),
    UNEVEN_SHOULDERS("肩膀不平衡", IssueSeverity.MEDIUM),
    EXCESSIVE_BACK_LEAN("身體過度後傾", IssueSeverity.HIGH),
    EXCESSIVE_FORWARD_LEAN("身體過度前傾", IssueSeverity.HIGH),
    
    // 下身相關
    UNEVEN_HIPS("臀部不平衡", IssueSeverity.MEDIUM),
    WEAK_KNEE_DRIVE("膝蓋驅動力不足", IssueSeverity.MEDIUM),
    
    // 手臂相關
    ASYMMETRIC_ARM_SWING("手臂擺動不對稱", IssueSeverity.MEDIUM),
    INSUFFICIENT_ARM_SWING("手臂擺動不足", IssueSeverity.MEDIUM),
    CROSSED_ARM_SWING("手臂過度交叉", IssueSeverity.LOW),
    
    // 步態相關
    SHUFFLING_GAIT("步態不清晰", IssueSeverity.MEDIUM),
    UNEVEN_STRIDE("步幅不均勻", IssueSeverity.MEDIUM)
}

/**
 * 問題嚴重程度
 */
enum class IssueSeverity {
    LOW,    // 低
    MEDIUM, // 中
    HIGH    // 高
}

/**
 * 整體分析總結
 */
data class AnalysisSummary(
    val overallScore: Float,                    // 整體分數 (0-100)
    val commonIssues: List<PostureIssue>,      // 常見問題
    val keyRecommendations: List<String>,      // 關鍵建議
    val totalFramesAnalyzed: Int,               // 分析的總幀數
    val analysisDate: Long = System.currentTimeMillis()
)
