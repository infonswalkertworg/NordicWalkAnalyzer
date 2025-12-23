package com.nordicwalk.analyzer.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StudentListPlaceholder(
    onNavigateToDetail: (Long) -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Student List Screen (Part A)")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onNavigateToDetail(0L) }) {
                Text("Add New Student")
            }
        }
    }
}

@Composable
fun StudentFormPlaceholder(
    onNavigateBack: () -> Unit,
    isEditing: Boolean = false,
    studentId: Long = 0L
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text(if (isEditing) "Edit Student (ID: $studentId)" else "Add Student")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateBack) {
                Text("Back")
            }
        }
    }
}

@Composable
fun StudentDetailPlaceholder(
    studentId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToForm: (Long) -> Unit,
    onNavigateToTrainingForm: (Long) -> Unit,
    onNavigateToAnalysis: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Student Detail (ID: $studentId)")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Training Records will be listed here")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onNavigateToForm(studentId) }) {
                Text("Edit Student")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = { onNavigateToTrainingForm(0L) }) {
                Text("Add Training Record")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateToAnalysis) {
                Text("Go to Video Analysis")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onNavigateBack) {
                Text("Back")
            }
        }
    }
}

@Composable
fun AnalysisHomePlaceholder(
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Video Analysis (Part B & C)")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onNavigateBack) {
                Text("Back to Students")
            }
        }
    }
}
