package com.nordicwalk.feature.student.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.core.data.repository.StudentRepository
import com.nordicwalk.core.data.repository.TrainingRecordRepository
import com.nordicwalk.core.domain.model.StudentDomain
import com.nordicwalk.core.domain.model.TrainingRecordDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    private val trainingRecordRepository: TrainingRecordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studentId: Long = savedStateHandle.get<Long>("studentId") ?: 0L

    // State flows
    private val _student = MutableStateFlow<StudentDomain?>(null)
    val student: StateFlow<StudentDomain?> = _student.asStateFlow()

    private val _trainingRecords = MutableStateFlow<List<TrainingRecordDomain>>(emptyList())
    val trainingRecords: StateFlow<List<TrainingRecordDomain>> = _trainingRecords.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadStudent()
        loadTrainingRecords()
    }

    fun loadStudent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val student = studentRepository.getStudentById(studentId)
                _student.value = student
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load student: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadTrainingRecords() {
        viewModelScope.launch {
            try {
                val records = trainingRecordRepository.getTrainingRecordsByStudent(studentId)
                // Sort by date (newer first)
                _trainingRecords.value = records.sortedByDescending { it.date }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load training records: ${e.message}"
            }
        }
    }

    fun deleteTrainingRecord(recordId: Long) {
        viewModelScope.launch {
            try {
                trainingRecordRepository.deleteTrainingRecord(recordId)
                loadTrainingRecords() // Reload list
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete training record: ${e.message}"
            }
        }
    }

    fun deleteStudent() {
        viewModelScope.launch {
            try {
                studentRepository.deleteStudent(studentId)
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete student: ${e.message}"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }
}
