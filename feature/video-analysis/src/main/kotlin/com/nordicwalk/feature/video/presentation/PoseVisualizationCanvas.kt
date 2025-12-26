package com.nordicwalk.feature.video.presentation

import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas

/**
 * 姿態可視化 Canvas (適配新版 PosePoint)
 */
@Composable
fun PoseVisualizationCanvas(
    bitmap: Bitmap,
    posePoints: List<PosePoint>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawIntoCanvas { canvas ->
            val imageWidth = bitmap.width.toFloat()
            val imageHeight = bitmap.height.toFloat()
            val canvasWidth = size.width
            val canvasHeight = size.height

            val scaleX = canvasWidth / imageWidth
            val scaleY = canvasHeight / imageHeight
            val scale = minOf(scaleX, scaleY)

            val scaledWidth = imageWidth * scale
            val scaledHeight = imageHeight * scale
            val offsetX = (canvasWidth - scaledWidth) / 2
            val offsetY = (canvasHeight - scaledHeight) / 2

            canvas.nativeCanvas.drawBitmap(
                bitmap,
                null,
                RectF(offsetX, offsetY, offsetX + scaledWidth, offsetY + scaledHeight),
                null
            )

            if (posePoints.isEmpty()) return@drawIntoCanvas

            val skeletonConnections = listOf(
                Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 7),
                Pair(0, 4), Pair(4, 5), Pair(5, 6), Pair(6, 8),
                Pair(9, 10), Pair(11, 12), Pair(11, 13), Pair(13, 15),
                Pair(12, 14), Pair(14, 16), Pair(11, 23), Pair(12, 24),
                Pair(23, 24), Pair(23, 25), Pair(25, 27), Pair(24, 26),
                Pair(26, 28), Pair(15, 17), Pair(15, 19), Pair(15, 21),
                Pair(16, 18), Pair(16, 20), Pair(16, 22), Pair(27, 29),
                Pair(29, 31), Pair(27, 31), Pair(28, 30), Pair(30, 32),
                Pair(28, 32)
            )

            val linePaint = Paint().apply {
                color = android.graphics.Color.GREEN
                strokeWidth = 4f * scale
                isAntiAlias = true
            }

            for ((startIdx, endIdx) in skeletonConnections) {
                if (startIdx < posePoints.size && endIdx < posePoints.size) {
                    val startPoint = posePoints[startIdx]
                    val endPoint = posePoints[endIdx]

                    if (startPoint.confidence > 0.3f && endPoint.confidence > 0.3f) {
                        // === 修正：使用 x, y 屬性，並乘上圖片寬高 ===
                        // 因為 PosePoint 現在存的是歸一化座標 (0~1)
                        val startX = offsetX + startPoint.x * imageWidth * scale
                        val startY = offsetY + startPoint.y * imageHeight * scale
                        val endX = offsetX + endPoint.x * imageWidth * scale
                        val endY = offsetY + endPoint.y * imageHeight * scale

                        canvas.nativeCanvas.drawLine(startX, startY, endX, endY, linePaint)
                    }
                }
            }

            val circlePaint = Paint().apply {
                color = android.graphics.Color.RED
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            for (point in posePoints) {
                if (point.confidence > 0.3f) {
                    // === 修正：使用 x, y 屬性 ===
                    val x = offsetX + point.x * imageWidth * scale
                    val y = offsetY + point.y * imageHeight * scale
                    val radius = (4f + point.confidence * 4f) * scale

                    canvas.nativeCanvas.drawCircle(x, y, radius, circlePaint)
                }
            }
        }
    }
}
