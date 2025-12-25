package com.nordicwalk.feature.video.presentation

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
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoRecordingScreen(
    onVideoRecorded: (videoPath: String) -> Unit,
    viewModel: VideoRecordingViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val recordedVideoPath by viewModel.recordedVideoPath.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    
    var displayDuration by remember { mutableStateOf(0L) }
    
    // 更新時間顯示
    LaunchedEffect(recordingDuration, isRecording) {
        if (isRecording) {
            delay(100)
            viewModel.updateRecordingDuration()
            displayDuration = recordingDuration
        }
    }
    
    // 處理錄製完成
    LaunchedEffect(recordedVideoPath) {
        recordedVideoPath?.let {
            onVideoRecorded(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 標題
        Text(
            text = "影片錄製",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // 預留攝像頭預覽區域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.DarkGray)
                .padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isRecording) {
                Text(
                    text = "📹 正在錄製中...",
                    color = Color.White,
                    fontSize = 18.sp
                )
            } else {
                Text(
                    text = "📷 攝像頭預覽\n(需要設定相機權限)",
                    color = Color.LightGray,
                    fontSize = 16.sp
                )
            }
        }
        
        // 錄製時間
        Text(
            text = "時間: ${formatDuration(displayDuration)}",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // 錯誤提示
        if (errorMessage != null) {
            Text(
                text = "❌ $errorMessage",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(10.dp)
            )
        }
        
        // 狀態信息
        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        
        // 按鈕區域
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isRecording) {
                // 停止錄製按鈕
                Button(
                    onClick = {
                        viewModel.stopRecording()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "停止",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White
                    )
                    Text("停止", modifier = Modifier.padding(start = 8.dp))
                }
                
                // 取消按鈕
                Button(
                    onClick = {
                        viewModel.cancelRecording()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 5.dp)
                ) {
                    Text("取消")
                }
            } else {
                // 開始錄製按鈕
                Button(
                    onClick = {
                        viewModel.startRecording()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .padding(horizontal = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "開始",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Red
                    )
                    Text("開始錄製", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }
}

/**
 * 格式化時間顯示
 */
private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
