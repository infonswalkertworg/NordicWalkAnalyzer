package com.nordicwalk.feature.student.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 計算北歐式健走杖建議長度
 * 公式: 身高(cm) × 0.68
 * 入門建議值: 計算結果往下取至最近的 5 的倍數
 * 進階建議值: 計算結果往上進位至最近的 5 的倍數
 *
 * 範例: 身高 180cm
 * 計算: 180 × 0.68 = 122.4cm
 * 入門建議: 120cm (往下)
 * 進階建議: 125cm (往上)
 */
object PoleLengthCalculator {
    /**
     * 根據身高計算入門建議杖長
     * @param heightCm 身高 (公分)
     * @return 入門建議杖長 (公分)
     */
    fun calculateBeginnerLength(heightCm: Int): Int {
        if (heightCm <= 0) return 0
        val baseLine = (heightCm * 0.68).toInt()
        // 往下取至最近的 5 的倍數
        return (baseLine / 5) * 5
    }

    /**
     * 根據身高計算進階建議杖長
     * @param heightCm 身高 (公分)
     * @return 進階建議杖長 (公分)
     */
    fun calculateAdvancedLength(heightCm: Int): Int {
        if (heightCm <= 0) return 0
        val baseLine = (heightCm * 0.68).toFloat()
        // 往上進位至最近的 5 的倍數
        return (((baseLine + 4.99f) / 5).toInt() * 5)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen(
    onNavigateBack: () -> Unit,
    viewModel: StudentFormViewModel = hiltViewModel()
) {
    val student = viewModel.student.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value
    val error = viewModel.error.collectAsState().value
    val isSaved = viewModel.isSaved.collectAsState().value

    // 根據身高計算杖長建議
    val beginnerPoleLength = PoleLengthCalculator.calculateBeginnerLength(student.heightCm)
    val advancedPoleLength = PoleLengthCalculator.calculateAdvancedLength(student.heightCm)

    LaunchedEffect(isSaved) {
        if (isSaved) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (student.id > 0) "編輯學員" else "新增學員") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "返回")
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (error != null) {
                    Text("錯誤: $error")
                }

                TextField(
                    value = student.firstName,
                    onValueChange = { viewModel.updateFirstName(it) },
                    label = { Text("名字") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TextField(
                    value = student.lastName,
                    onValueChange = { viewModel.updateLastName(it) },
                    label = { Text("姓氏") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                TextField(
                    value = if (student.age > 0) student.age.toString() else "",
                    onValueChange = { 
                        val age = it.toIntOrNull() ?: 0
                        viewModel.updateAge(age)
                    },
                    label = { Text("年齡") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // 身高輸入框
                TextField(
                    value = if (student.heightCm > 0) student.heightCm.toString() else "",
                    onValueChange = { 
                        val height = it.toIntOrNull() ?: 0
                        viewModel.updateHeight(height)
                    },
                    label = { Text("身高 (公分)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                // 杖長建議顯示區域
                if (student.heightCm > 0) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🎯 北歐式健走杖建議長度",
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        
                        // 計算過程
                        Text(
                            text = "計算公式: ${student.heightCm} cm × 0.68 = ${String.format("%.1f", student.heightCm * 0.68)} cm",
                            modifier = Modifier.padding(start = 8.dp),
                            fontSize = 12.sp
                        )

                        // 入門建議
                        Text(
                            text = "✓ 入門建議: $beginnerPoleLength cm (往下取整至 5 的倍數)",
                            modifier = Modifier.padding(start = 8.dp)
                        )

                        // 進階建議
                        Text(
                            text = "✓ 進階建議: $advancedPoleLength cm (往上進位至 5 的倍數)",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                TextField(
                    value = student.notes,
                    onValueChange = { viewModel.updateNotes(it) },
                    label = { Text("備註") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Button(
                    onClick = { viewModel.saveStudent() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    enabled = student.firstName.isNotBlank() && student.lastName.isNotBlank() && student.age > 0
                ) {
                    Text("儲存")
                }
            }
        }
    }
}
