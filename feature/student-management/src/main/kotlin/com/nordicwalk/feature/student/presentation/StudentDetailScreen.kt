package com.nordicwalk.feature.student.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nordicwalk.feature.video.analysis.data.local.entity.AnalysisRecordEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToAnalysisResult: (Long) -> Unit, // 跳轉到看結果
    onRecordNewVideo: () -> Unit,               // 跳轉到錄影
    onImportVideo: () -> Unit,                  // 跳轉到匯入
    viewModel: StudentDetailViewModel = hiltViewModel()
) {
    val student by viewModel.student.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val analysisRecords by viewModel.analysisRecords.collectAsState() // ✅ 讀取分析紀錄

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("學員詳情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    student?.let {
                        TextButton(onClick = { onNavigateToEdit(it.id) }) {
                            Text("編輯")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("錯誤: $error")
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("返回")
                }
            }
        } else if (student != null) {
            // ✅ 改用 LazyColumn 以支援長列表
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. 學員基本資料區
                item {
                    StudentInfoCard(student!!)
                }

                // 2. 功能按鈕區 (錄影/匯入)
                item {
                    ActionButtonsRow(
                        onRecord = onRecordNewVideo,
                        onImport = onImportVideo
                    )
                }

                // 3. 歷史紀錄標題
                item {
                    Text(
                        "歷史分析紀錄",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 4. 歷史紀錄列表
                if (analysisRecords.isEmpty()) {
                    item {
                        EmptyHistoryView()
                    }
                } else {
                    items(analysisRecords) { record ->
                        AnalysisRecordCard(
                            record = record,
                            onClick = { onNavigateToAnalysisResult(record.id) }
                        )
                    }
                }
            }
        }
    }
}

// ✅ 拆分出學員資料卡片
@Composable
fun StudentInfoCard(student: com.nordicwalk.core.domain.model.Student) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${student.firstName} ${student.lastName}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("年齡: ${student.age} 歲", style = MaterialTheme.typography.bodyLarge)
                if (student.heightCm > 0) {
                    Text("身高: ${student.heightCm} cm", style = MaterialTheme.typography.bodyLarge)
                }
            }

            if (student.notes.isNotEmpty()) {
                Text(
                    text = "備註: ${student.notes}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ✅ 功能按鈕列
@Composable
fun ActionButtonsRow(onRecord: () -> Unit, onImport: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onRecord,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Videocam, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("即時錄製")
        }

        OutlinedButton(
            onClick = onImport,
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.FileUpload, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("匯入影片")
        }
    }
}

// ✅ 單筆紀錄卡片
@Composable
fun AnalysisRecordCard(
    record: AnalysisRecordEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左側縮圖區 (目前用 icon 代替)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 中間資訊
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.recordCode,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = record.analysisDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    color = if (record.analysisType == "REALTIME")
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = if (record.analysisType == "REALTIME") "即時錄製" else "匯入影片",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (record.analysisType == "REALTIME")
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else
                            MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // 右側分數
            if (record.overallScore > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${record.overallScore.toInt()}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text("分", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// ✅ 空紀錄顯示
@Composable
fun EmptyHistoryView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "尚無分析紀錄",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            "點擊上方按鈕開始第一次分析",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
