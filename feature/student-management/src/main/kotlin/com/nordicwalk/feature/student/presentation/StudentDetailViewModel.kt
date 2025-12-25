package com.nordicwalk.feature.student.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.core.data.repository.StudentRepository
import com.nordicwalk.core.domain.model.Student
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val studentId: Long = savedStateHandle["studentId"] ?: 0L

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        if (studentId > 0) {
            loadStudent()
        }
    }

    private fun loadStudent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loadedStudent = studentRepository.getStudentById(studentId)
                _student.value = loadedStudent
                if (loadedStudent == null) {
                    _error.value = "Student not found"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error loading student"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
