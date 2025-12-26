package com.nordicwalk.feature.video.presentation

import android.content.ContentValues
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoPlaybackScreen(
    videoPath: String?,
    onBack: () -> Unit,
    onAnalysisStart: () -> Unit,
    viewModel: VideoPlaybackViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentFrame by viewModel.currentFrame.collectAsState()
    val totalFrames by viewModel.totalFrames.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val frameBitmap by viewModel.frameBitmap.collectAsState()
    val posePoints by viewModel.posePoints.collectAsState()
    
    var showSpeedMenu by remember { mutableStateOf(false) }
    
    LaunchedEffect(videoPath) {
        videoPath?.let {
            viewModel.loadVideo(it)
        }
    }
    
    // 播放循環
    LaunchedEffect(isPlaying) {
        while (isPlaying && currentFrame < totalFrames) {
            val frameDelay = (1000L / (30 * playbackSpeed)).toLong()
            delay(frameDelay)
            viewModel.nextFrame()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 標題
        Text(
            text = "影片回放 - 姿態檢查",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 影片顯示區域
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                if (frameBitmap != null) {
                    // 顯示當前幀
                    PoseVisualizationCanvas(
                        bitmap = frameBitmap!!,
                        posePoints = posePoints,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = "載入中...",
                        color = Color.White
                    )
                }
            }
        }

        // 時間和進度條
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 時間顯示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatTime(currentFrame * 1000L / 30)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "x${String.format("%.2f", playbackSpeed)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${formatTime(totalFrames * 1000L / 30)}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // 進度條
            Slider(
                value = if (totalFrames > 0) currentFrame.toFloat() else 0f,
                onValueChange = { viewModel.seekFrame(it.toInt()) },
                valueRange = 0f..if (totalFrames > 0) totalFrames.toFloat() else 1f,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 播放控制按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一幀
            IconButton(onClick = { viewModel.previousFrame() }) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "上一幀"
                )
            }

            // 播放/暫停
            IconButton(
                onClick = { viewModel.togglePlayPause() },
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暫停" else "播放",
                    tint = Color.White
                )
            }

            // 下一幀
            IconButton(onClick = { viewModel.nextFrame() }) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "下一幀"
                )
            }

            // 截圖
            IconButton(onClick = {
                frameBitmap?.let {
                    viewModel.captureFrame(context, it)
                }
            }) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "截圖"
                )
            }
        }

        // 播放速度選擇
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "播放速度",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                speeds.forEach { speed ->
                    Button(
                        onClick = { viewModel.setPlaybackSpeed(speed) },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        enabled = playbackSpeed != speed
                    ) {
                        Text(
                            text = if (speed == 1.0f) "正常" else "${String.format("%.2f", speed)}x",
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 底部按鈕
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(45.dp)
            ) {
                Text("返回")
            }
            Button(
                onClick = onAnalysisStart,
                modifier = Modifier
                    .weight(1f)
                    .height(45.dp)
            ) {
                Text("開始分析")
            }
        }
    }
}

private fun formatTime(timeMs: Long): String {
    val totalSeconds = timeMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
