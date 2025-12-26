package com.nordicwalk.feature.video.presentation

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.PorterDuff
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

/**
 * 姿態可視化 Canvas
 * 繪制檔案堻态和人体骨架
 */
@Composable
fun PoseVisualizationCanvas(
    bitmap: Bitmap,
    posePoints: List<PosePoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawIntoCanvas { canvas ->
            // 繪制位圖片
            val imageWidth = bitmap.width
            val imageHeight = bitmap.height
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val scaleX = canvasWidth / imageWidth
            val scaleY = canvasHeight / imageHeight
            
            canvas.nativeCanvas.drawBitmap(
                bitmap,
                0f,
                0f,
                null
            )
            
            // 视丢上通常的人体骨架連接关系
            val skeletonConnections = listOf(
                // 上半身
                Pair(0, 1),   // 鼻子 - 左上矩
                Pair(0, 2),   // 鼻子 - 右上矩
                Pair(1, 3),   // 左上矩 - 左耳
                Pair(2, 4),   // 右上矩 - 右耳
                Pair(1, 5),   // 左上矩 - 左肃啃
                Pair(2, 6),   // 右上矩 - 右肃啃
                Pair(5, 7),   // 左肃啃 - 左手膁
                Pair(6, 8),   // 右肃啃 - 右手膁
                Pair(7, 9),   // 左手膁 - 左手
                Pair(8, 10),  // 右手膁 - 右手
                
                // 下半身
                Pair(11, 12), // 左騷 - 右騷
                Pair(5, 11),  // 左肃啃 - 左騷
                Pair(6, 12),  // 右肃啃 - 右騷
                Pair(11, 13), // 左騷 - 左膝盛
                Pair(12, 14), // 右騷 - 右膝盛
                Pair(13, 15), // 左膝盛 - 左脚踛
                Pair(14, 16), // 右膝盛 - 右脚踛
                Pair(15, 17), // 左脚踛 - 左脚提
                Pair(16, 18)  // 右脚踛 - 右脚提
            )
            
            // 選擇有效的點適
            val validPoints = posePoints.filter { it.confidence > 0.3f }
            
            // 繪制骨架連接
            val linePaint = Paint().apply {
                color = android.graphics.Color.GREEN
                strokeWidth = 3f
                isAntiAlias = true
            }
            
            for ((startIdx, endIdx) in skeletonConnections) {
                val startPoint = validPoints.find { it.label == getKeyPointName(startIdx) }
                val endPoint = validPoints.find { it.label == getKeyPointName(endIdx) }
                
                if (startPoint != null && endPoint != null) {
                    canvas.nativeCanvas.drawLine(
                        startPoint.x,
                        startPoint.y,
                        endPoint.x,
                        endPoint.y,
                        linePaint
                    )
                }
            }
            
            // 繪制关节点
            val circlePaint = Paint().apply {
                color = android.graphics.Color.RED
                strokeWidth = 2f
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            
            for (point in validPoints) {
                // 基于信心度调整频开度大小
                val radius = 4f + (point.confidence * 3f)
                canvas.nativeCanvas.drawCircle(
                    point.x,
                    point.y,
                    radius,
                    circlePaint
                )
            }
            
            // 可選: 顯示关节不稳定校警
            drawUnstableJoints(canvas.nativeCanvas, validPoints)
        }
    }
}

private fun drawUnstableJoints(
    canvas: android.graphics.Canvas,
    points: List<PosePoint>
) {
    val unstableThreshold = 0.5f
    val warningPaint = Paint().apply {
        color = android.graphics.Color.YELLOW
        strokeWidth = 3f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }
    
    for (point in points) {
        if (point.confidence < unstableThreshold) {
            canvas.drawCircle(point.x, point.y, 8f, warningPaint)
        }
    }
}

private fun getKeyPointName(index: Int): String {
    return when (index) {
        0 -> "nose"
        1 -> "left_eye"
        2 -> "right_eye"
        3 -> "left_ear"
        4 -> "right_ear"
        5 -> "left_shoulder"
        6 -> "right_shoulder"
        7 -> "left_elbow"
        8 -> "right_elbow"
        9 -> "left_wrist"
        10 -> "right_wrist"
        11 -> "left_hip"
        12 -> "right_hip"
        13 -> "left_knee"
        14 -> "right_knee"
        15 -> "left_ankle"
        16 -> "right_ankle"
        17 -> "left_foot_index"
        18 -> "right_foot_index"
        else -> "unknown"
    }
}
