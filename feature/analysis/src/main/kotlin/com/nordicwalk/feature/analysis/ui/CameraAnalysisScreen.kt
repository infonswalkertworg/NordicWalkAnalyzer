package com.nordicwalk.feature.analysis.ui

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nordicwalk.feature.analysis.presentation.CameraAnalysisViewModel
import com.nordicwalk.feature.analysis.presentation.CameraAnalysisUiState
import com.nordicwalk.core.domain.model.ViewDirection
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraAnalysisScreen(
    viewModel: CameraAnalysisViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToResult: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val currentFrame by viewModel.currentFrame.collectAsStateWithLifecycle()
    val frameCount by viewModel.frameCount.collectAsStateWithLifecycle()
    val fps by viewModel.fps.collectAsStateWithLifecycle()
    val currentDirection by viewModel.currentDirection.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProvider = remember { mutableStateOf<ProcessCameraProvider?>(null) }

    // Initialize camera
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            cameraProvider.value = provider
            setupCamera(provider, previewView, lifecycleOwner, context)
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraProvider.value?.unbindAll()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pose Analysis - ${currentDirection.name}") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            // Camera Preview
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            // Overlay with metrics and controls
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top - Status and Metrics
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Recording status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (isRecording) {
                                Icon(
                                    Icons.Filled.FiberManualRecord,
                                    "Recording",
                                    tint = Color.Red,
                                    modifier = Modifier.width(12.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "RECORDING",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                            } else {
                                Text(
                                    "Ready",
                                    color = Color.Gray,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                            }
                        }

                        // FPS
                        Text(
                            "${String.format("%.1f", fps)} FPS",
                            color = Color.White,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                        )

                        // Frame count
                        Text(
                            "Frame: $frameCount",
                            color = Color.White,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize
                        )
                    }
                }

                Spacer(modifier = Modifier.height(1.dp))

                // Bottom - Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.Black.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    // Direction selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ViewDirection.values().forEach { direction ->
                            Button(
                                onClick = { viewModel.setDirection(direction) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    direction.name,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize
                                )
                            }
                        }
                    }

                    // Recording controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.toggleRecording() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                if (isRecording) Icons.Filled.Stop else Icons.Filled.FiberManualRecord,
                                "Toggle Recording",
                                modifier = Modifier.width(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isRecording) "Stop" else "Start")
                        }

                        Button(
                            onClick = { viewModel.discardSession() },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Discard")
                        }
                    }
                }
            }
        }

        // Handle state changes
        LaunchedEffect(uiState) {
            when (uiState) {
                is CameraAnalysisUiState.SessionSaved -> {
                    onNavigateToResult((uiState as CameraAnalysisUiState.SessionSaved).sessionId)
                }
                is CameraAnalysisUiState.Error -> {
                    // Handle error - could show snackbar
                }
                else -> {}
            }
        }
    }
}

private fun setupCamera(
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    context: android.content.Context
) {
    val preview = Preview.Builder().build().also {
        it.setSurfaceProvider(previewView.surfaceProvider)
    }

    val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

    val imageAnalysis = ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { analysis ->
            analysis.setAnalyzer(
                Executors.newSingleThreadExecutor(),
                { imageProxy: ImageProxy ->
                    // Process frame here
                    imageProxy.close()
                }
            )
        }

    try {
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalysis
        )
    } catch (exc: Exception) {
        // Handle exception
    }
}

@Composable
fun CameraAnalysisPlaceholder(
    studentId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToResult: (Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text("Camera Analysis Screen (Student: $studentId)", color = Color.White)
    }
}
