package com.nordicwalk.feature.student.presentation

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.core.data.repository.TrainingRecordRepository
import com.nordicwalk.core.domain.model.TrainingRecordDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TrainingRecordFormViewModel @Inject constructor(
    private val trainingRecordRepository: TrainingRecordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studentId: Long = savedStateHandle.get<Long>("studentId") ?: 0L
    private val recordId: Long = savedStateHandle.get<Long>("recordId") ?: 0L

    // Form state
    private val _date = MutableStateFlow(LocalDate.now())
    val date: StateFlow<LocalDate> = _date.asStateFlow()

    private val _startTime = MutableStateFlow(LocalTime.of(9, 0))
    val startTime: StateFlow<LocalTime> = _startTime.asStateFlow()

    private val _endTime = MutableStateFlow(LocalTime.of(10, 0))
    val endTime: StateFlow<LocalTime> = _endTime.asStateFlow()

    private val _distance = MutableStateFlow("5.0")
    val distance: StateFlow<String> = _distance.asStateFlow()

    private val _avgHeartRate = MutableStateFlow("")
    val avgHeartRate: StateFlow<String> = _avgHeartRate.asStateFlow()

    private val _maxHeartRate = MutableStateFlow("")
    val maxHeartRate: StateFlow<String> = _maxHeartRate.asStateFlow()

    private val _vo2Max = MutableStateFlow("")
    val vo2Max: StateFlow<String> = _vo2Max.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _improveNotes = MutableStateFlow("")
    val improveNotes: StateFlow<String> = _improveNotes.asStateFlow()

    private val _screenshotUris = MutableStateFlow<List<Uri>>(emptyList())
    val screenshotUris: StateFlow<List<Uri>> = _screenshotUris.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isEditing = MutableStateFlow(false)
    val isEditing: StateFlow<Boolean> = _isEditing.asStateFlow()

    init {
        if (recordId > 0) {
            loadRecord(recordId)
        }
    }

    fun loadRecord(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val record = trainingRecordRepository.getTrainingRecordById(id)
                if (record != null) {
                    _date.value = record.date
                    _startTime.value = record.startTime
                    _endTime.value = record.endTime
                    _distance.value = record.distance.toString()
                    _avgHeartRate.value = record.avgHeartRate?.toString() ?: ""
                    _maxHeartRate.value = record.maxHeartRate?.toString() ?: ""
                    _vo2Max.value = record.vo2Max?.toString() ?: ""
                    _description.value = record.description
                    _improveNotes.value = record.improveNotes
                    _screenshotUris.value = record.screenshotUris
                    _isEditing.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load record: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateDate(newDate: LocalDate) {
        if (newDate <= LocalDate.now()) {
            _date.value = newDate
            _errorMessage.value = null
        } else {
            _errorMessage.value = "Cannot select future date"
        }
    }

    fun updateStartTime(newTime: LocalTime) {
        _startTime.value = newTime
        _errorMessage.value = null
    }

    fun updateEndTime(newTime: LocalTime) {
        if (newTime > _startTime.value) {
            _endTime.value = newTime
            _errorMessage.value = null
        } else {
            _errorMessage.value = "End time must be after start time"
        }
    }

    fun updateDistance(km: String) {
        _distance.value = km
        validateDistance(km)
    }

    fun updateAvgHeartRate(bpm: String) {
        _avgHeartRate.value = bpm
        validateHeartRate(bpm)
    }

    fun updateMaxHeartRate(bpm: String) {
        _maxHeartRate.value = bpm
        validateHeartRate(bpm)
    }

    fun updateVO2Max(value: String) {
        _vo2Max.value = value
        if (value.isEmpty() || value.toDoubleOrNull() != null) {
            _errorMessage.value = null
        }
    }

    fun updateDescription(text: String) {
        _description.value = text
    }

    fun updateImproveNotes(text: String) {
        _improveNotes.value = text
    }

    fun addScreenshot(uri: Uri) {
        val currentUris = _screenshotUris.value.toMutableList()
        if (!currentUris.contains(uri)) {
            currentUris.add(uri)
            _screenshotUris.value = currentUris
        }
    }

    fun removeScreenshot(index: Int) {
        val currentUris = _screenshotUris.value.toMutableList()
        if (index in currentUris.indices) {
            currentUris.removeAt(index)
            _screenshotUris.value = currentUris
        }
    }

    fun saveRecord() {
        if (!validateInput()) return

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val record = TrainingRecordDomain(
                    id = if (_isEditing.value) recordId else 0L,
                    studentId = studentId,
                    date = _date.value,
                    startTime = _startTime.value,
                    endTime = _endTime.value,
                    distance = _distance.value.toDouble(),
                    avgHeartRate = _avgHeartRate.value.toIntOrNull(),
                    maxHeartRate = _maxHeartRate.value.toIntOrNull(),
                    vo2Max = _vo2Max.value.toDoubleOrNull(),
                    description = _description.value,
                    improveNotes = _improveNotes.value,
                    screenshotUris = _screenshotUris.value
                )

                if (_isEditing.value) {
                    trainingRecordRepository.updateTrainingRecord(record)
                } else {
                    trainingRecordRepository.insertTrainingRecord(record)
                }

                _saveSuccess.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save record: ${e.message}"
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRecord() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                trainingRecordRepository.deleteTrainingRecord(recordId)
                _saveSuccess.value = true
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete record: ${e.message}"
                _saveSuccess.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun validateInput(): Boolean {
        when {
            _date.value > LocalDate.now() -> {
                _errorMessage.value = "Date cannot be in the future"
                return false
            }
            _startTime.value >= _endTime.value -> {
                _errorMessage.value = "End time must be after start time"
                return false
            }
            _distance.value.isEmpty() || _distance.value.toDoubleOrNull() == null -> {
                _errorMessage.value = "Distance must be a valid number"
                return false
            }
            _distance.value.toDouble() < 0.1 -> {
                _errorMessage.value = "Distance must be at least 0.1 km"
                return false
            }
            else -> {
                _errorMessage.value = null
                return true
            }
        }
    }

    private fun validateDistance(km: String) {
        if (km.isNotEmpty()) {
            val distance = km.toDoubleOrNull()
            if (distance == null) {
                _errorMessage.value = "Distance must be a valid number"
            } else if (distance < 0.1) {
                _errorMessage.value = "Distance must be at least 0.1 km"
            } else {
                _errorMessage.value = null
            }
        }
    }

    private fun validateHeartRate(bpm: String) {
        if (bpm.isNotEmpty()) {
            val hr = bpm.toIntOrNull()
            if (hr == null) {
                _errorMessage.value = "Heart rate must be a valid number"
            } else if (hr < 40 || hr > 220) {
                _errorMessage.value = "Heart rate should be between 40 and 220"
            } else {
                _errorMessage.value = null
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun resetSaveSuccess() {
        _saveSuccess.value = false
    }
}
