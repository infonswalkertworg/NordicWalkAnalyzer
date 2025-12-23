package com.nordicwalk.feature.student.presentation.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.nordicwalk.feature.student.presentation.TrainingRecordFormViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingRecordFormScreen(
    viewModel: TrainingRecordFormViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    isEditing: Boolean = false
) {
    val date by viewModel.date.collectAsStateWithLifecycle()
    val startTime by viewModel.startTime.collectAsStateWithLifecycle()
    val endTime by viewModel.endTime.collectAsStateWithLifecycle()
    val distance by viewModel.distance.collectAsStateWithLifecycle()
    val avgHeartRate by viewModel.avgHeartRate.collectAsStateWithLifecycle()
    val maxHeartRate by viewModel.maxHeartRate.collectAsStateWithLifecycle()
    val vo2Max by viewModel.vo2Max.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val improveNotes by viewModel.improveNotes.collectAsStateWithLifecycle()
    val screenshotUris by viewModel.screenshotUris.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val saveSuccess by viewModel.saveSuccess.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Training Record" else "Add Training Record") },
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (saveSuccess) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Training record saved successfully!",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Return")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(16.dp)
            ) {
                item {
                    // Error Message
                    if (errorMessage != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Date Field (Read-only, click to select)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp)
                            .clickable { /* Show date picker */ }
                    ) {
                        Text("Date: ${date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))}")
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Start Time
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp)
                            .clickable { /* Show time picker */ }
                    ) {
                        Text("Start: ${startTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // End Time
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(12.dp)
                            .clickable { /* Show time picker */ }
                    ) {
                        Text("End: ${endTime.format(DateTimeFormatter.ofPattern("HH:mm"))}")
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Distance
                    TextField(
                        value = distance,
                        onValueChange = viewModel::updateDistance,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Distance (km)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Avg Heart Rate
                    TextField(
                        value = avgHeartRate,
                        onValueChange = viewModel::updateAvgHeartRate,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Avg Heart Rate (bpm)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Max Heart Rate
                    TextField(
                        value = maxHeartRate,
                        onValueChange = viewModel::updateMaxHeartRate,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Max Heart Rate (bpm)") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // VO2 Max
                    TextField(
                        value = vo2Max,
                        onValueChange = viewModel::updateVO2Max,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("VO2 Max") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Description
                    TextField(
                        value = description,
                        onValueChange = viewModel::updateDescription,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        label = { Text("Description") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Improvement Notes
                    TextField(
                        value = improveNotes,
                        onValueChange = viewModel::updateImproveNotes,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        label = { Text("Improvement Notes") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Screenshots
                    if (screenshotUris.isNotEmpty()) {
                        Text("Screenshots (${screenshotUris.size})", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                items(screenshotUris) { uri ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(4.dp)
                    ) {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Screenshot",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        IconButton(
                            onClick = { viewModel.removeScreenshot(screenshotUris.indexOf(uri)) },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(MaterialTheme.colorScheme.error)
                                .size(32.dp)
                        ) {
                            Icon(Icons.Filled.Close, "Remove", tint = MaterialTheme.colorScheme.onError)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { /* Launch image picker */ },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Filled.Add, "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Screenshot")
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    // Save Button
                    Button(
                        onClick = viewModel::saveRecord,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(if (isEditing) "Update" else "Save")
                    }

                    if (isEditing) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = viewModel::deleteRecord,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Delete Record")
                        }
                    }
                }
            }
        }
    }
}
