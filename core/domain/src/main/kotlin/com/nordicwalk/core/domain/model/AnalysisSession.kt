package com.nordicwalk.core.domain.model

enum class CaptureSource {
    CAMERA, IMPORTED_VIDEO
}

enum class ViewDirection {
    FRONT,  // 正面
    BACK,   // 背面
    LEFT,   // 左側（向畫面左側行進）
    RIGHT   // 右側（向畫面右側行進）
}

data class AnalysisSession(
    val id: Long = 0L,
    val studentId: Long? = null,
    val source: CaptureSource,
    val videoUri: String? = null,
    val direction: ViewDirection = ViewDirection.FRONT,
    val actualDirection: ViewDirection? = null,
    val metricsJson: String? = null,
    val reportText: String? = null,
    val thumbnailUri: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isValid: Boolean
        get() = source != null

    val displayDirection: String
        get() = when (actualDirection ?: direction) {
            ViewDirection.FRONT -> "正面"
            ViewDirection.BACK -> "背面"
            ViewDirection.LEFT -> "左側"
            ViewDirection.RIGHT -> "右側"
        }
}