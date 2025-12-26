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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle

@Composable
fun VideoAnalysisScreen(
    videoPath: String?,
    onBack: () -> Unit,
    onAnalysisComplete: ((videoPath: String, summary: com.nordicwalk.feature.video.domain.model.AnalysisSummary) -> Unit)? = null,
    viewModel: VideoAnalysisViewModel = hiltViewModel()
) {
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val analysisProgress by viewModel.analysisProgress.collectAsState()
    val analysisResults by viewModel.analysisResults.collectAsState()
    val analysisSummary by viewModel.analysisSummary.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val videoDuration by viewModel.videoDuration.collectAsState()

    // 當分析完成時觸發回調
    LaunchedEffect(analysisSummary) {
        analysisSummary?.let { summary ->
            videoPath?.let { path ->
                onAnalysisComplete?.invoke(path, summary)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 標題
        Text(
            text = "動作分析",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 訓練記錄信息
        if (!videoPath.isNullOrEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "檔案: ${videoPath.substringAfterLast('/')}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "時間: ${formatDuration(videoDuration)}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // 錯誤提示
        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "❌ $errorMessage",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // 分析進度
        if (isAnalyzing) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "動作分析中... ${(analysisProgress * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                LinearProgressIndicator(
                    progress = analysisProgress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                )
            }
        } else if (analysisSummary != null) {
            // 分析結果
            val summary = analysisSummary!!
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 總体評分
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "整體評分",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = String.format("%.1f / 100", summary.overallScore),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                summary.overallScore >= 80f -> Color.Green
                                summary.overallScore >= 60f -> Color.Yellow
                                else -> Color.Red
                            }
                        )
                        Text(
                            text = "分析幀數: ${summary.totalFramesAnalyzed}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }

                // 偵測問題
                if (summary.commonIssues.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "偵測問題",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            summary.commonIssues.forEach { issue ->
                                Text(
                                    text = "• ${issue.description}",
                                    fontSize = 12.sp,
                                    color = when (issue.severity) {
                                        com.nordicwalk.feature.video.domain.model.IssueSeverity.HIGH -> Color.Red
                                        com.nordicwalk.feature.video.domain.model.IssueSeverity.MEDIUM -> Color.Yellow
                                        com.nordicwalk.feature.video.domain.model.IssueSeverity.LOW -> Color.Green
                                    }
                                )
                            }
                        }
                    }
                }

                // 建議
                if (summary.keyRecommendations.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "改善建議",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            summary.keyRecommendations.forEach { recommendation ->
                                Text(
                                    text = "⚡ $recommendation",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // 下一步指引
            Text(
                text = "點擊下記按鈕開始分析",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 30.dp)
            )
        }

        // 按鈕區
        if (!videoPath.isNullOrEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 分析完成後顯示「完成」按鈕，否則顯示「開始分析」
                if (analysisSummary != null) {
                    Button(
                        onClick = onBack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(45.dp)
                    ) {
                        Text("完成")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.analyzeVideo(framesPerSecond = 5)
                        },
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "開始分析",
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text("開始分析")
                    }

                    Button(
                        onClick = onBack,
                        enabled = !isAnalyzing,
                        modifier = Modifier
                            .weight(1f)
                            .height(45.dp)
                    ) {
                        Text("返回")
                    }
                }
            }
        }
    }
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
