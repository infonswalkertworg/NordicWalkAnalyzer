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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentListUiState(
    val students: List<Student> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = ""
)

@HiltViewModel
class StudentListViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentListUiState())
    val uiState: StateFlow<StudentListUiState> = _uiState.asStateFlow()

    init {
        loadStudents()
    }

    fun loadStudents() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                studentRepository.getAllStudents()
                    .catch { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = exception.message
                            )
                        }
                    }
                    .collect { students ->
                        _uiState.update {
                            it.copy(
                                students = students,
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            try {
                studentRepository.deleteStudent(student)
                loadStudents()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message)
                }
            }
        }
    }

    fun searchStudents(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isBlank()) {
            loadStudents()
        } else {
            viewModelScope.launch {
                try {
                    studentRepository.searchStudents(query)
                        .catch { exception ->
                            _uiState.update {
                                it.copy(error = exception.message)
                            }
                        }
                        .collect { students ->
                            _uiState.update {
                                it.copy(students = students)
                            }
                        }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(error = e.message)
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
