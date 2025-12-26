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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.nordicwalk.feature.video.util.hasRequiredPermissions
import com.nordicwalk.feature.video.util.rememberPermissionState
import kotlinx.coroutines.delay

@Composable
fun VideoRecordingScreen(
    onVideoRecorded: (videoPath: String) -> Unit,
    viewModel: VideoRecordingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    
    val isRecording by viewModel.isRecording.collectAsState()
    val recordingDuration by viewModel.recordingDuration.collectAsState()
    val recordedVideoPath by viewModel.recordedVideoPath.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val isCameraReady by viewModel.isCameraReady.collectAsState()
    
    var displayDuration by remember { mutableStateOf(0L) }
    var showPermissionDenied by remember { mutableStateOf(false) }
    // 使用狀態追蹤權限，而不是直接調用函數
    var hasPermissions by remember { mutableStateOf(context.hasRequiredPermissions()) }
    
    // 權限狀態
    val permissionState = rememberPermissionState(
        onPermissionsGranted = {
            showPermissionDenied = false
            hasPermissions = true  // 更新權限狀態
        },
        onPermissionsDenied = {
            showPermissionDenied = true
            hasPermissions = false
        }
    )
    
    // 檢查權限
    LaunchedEffect(Unit) {
        hasPermissions = context.hasRequiredPermissions()
        if (!hasPermissions) {
            permissionState.requestPermissions()
        }
    }
    
    // 定期檢查權限狀態（當用戶從設定返回時）
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)  // 每秒檢查一次
            val currentPermissions = context.hasRequiredPermissions()
            if (currentPermissions != hasPermissions) {
                hasPermissions = currentPermissions
            }
        }
    }
    
    // 更新時間顯示
    LaunchedEffect(recordingDuration, isRecording) {
        if (isRecording) {
            delay(100)
            viewModel.updateRecordingDuration()
            displayDuration = recordingDuration
        }
    }
    
    // 處理錄影完成
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
            text = "影片錄影",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        // 相機預覽區域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                !hasPermissions -> {
                    // 權限未授予
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "⚠️ 需要相機和麥克風權限",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Button(onClick = { permissionState.requestPermissions() }) {
                            Text("授予權限")
                        }
                        if (showPermissionDenied) {
                            Text(
                                text = "請在系統設定中手動開啟權限",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
                else -> {
                    // 顯示相機預覽
                    CameraPreviewWithRecording(
                        modifier = Modifier.fillMaxSize(),
                        onVideoCaptureReady = { videoCapture ->
                            viewModel.setVideoCapture(videoCapture)
                        }
                    )
                    
                    // 相機初始化中
                    if (!isCameraReady) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(color = Color.White)
                                Text(
                                    text = "相機初始化中...",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                    
                    // 錄影指示器
                    if (isRecording) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(
                                            color = Color.Red,
                                            shape = androidx.compose.foundation.shape.CircleShape
                                        )
                                )
                                Text(
                                    text = "REC",
                                    color = Color.Red,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // 錄影時間
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
                // 停止錄影按鈕
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
                // 開始錄影按鈕
                Button(
                    onClick = {
                        viewModel.startRecording()
                    },
                    enabled = isCameraReady && hasPermissions,
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
                    Text("開始錄影", modifier = Modifier.padding(start = 8.dp))
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
