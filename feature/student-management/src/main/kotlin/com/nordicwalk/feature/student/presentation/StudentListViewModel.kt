package com.nordicwalk.feature.student.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.core.data.repository.StudentRepository
import com.nordicwalk.core.domain.model.Student
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _isLoading.value = true
            studentRepository.getAllStudents()
                .catch { e ->
                    _error.value = e.message ?: "Error loading students"
                    _isLoading.value = false
                }
                .collect { studentList ->
                    _students.value = studentList
                    _error.value = null
                    _isLoading.value = false
                }
        }
    }

    fun deleteStudent(studentId: Long) {
        viewModelScope.launch {
            try {
                studentRepository.deleteStudent(studentId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Error deleting student"
            }
        }
    }
}
