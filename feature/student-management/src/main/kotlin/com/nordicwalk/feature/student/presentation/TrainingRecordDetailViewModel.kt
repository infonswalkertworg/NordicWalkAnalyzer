package com.nordicwalk.feature.student.presentation

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
import javax.inject.Inject

@HiltViewModel
class TrainingRecordDetailViewModel @Inject constructor(
    private val trainingRecordRepository: TrainingRecordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recordId: Long = savedStateHandle.get<Long>("recordId") ?: 0L

    private val _record = MutableStateFlow<TrainingRecordDomain?>(null)
    val record: StateFlow<TrainingRecordDomain?> = _record.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadRecord()
    }

    fun loadRecord() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val record = trainingRecordRepository.getTrainingRecordById(recordId)
                _record.value = record
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load record: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRecord() {
        viewModelScope.launch {
            try {
                trainingRecordRepository.deleteTrainingRecord(recordId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete record: ${e.message}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
