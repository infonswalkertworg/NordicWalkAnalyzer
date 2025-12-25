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
class StudentFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository
) : ViewModel() {

    private val studentId: Long? = savedStateHandle["studentId"]

    private val _student = MutableStateFlow(Student.empty())
    val student: StateFlow<Student> = _student.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    init {
        if (studentId != null && studentId > 0) {
            loadStudent(studentId)
        }
    }

    private fun loadStudent(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loadedStudent = studentRepository.getStudentById(id)
                if (loadedStudent != null) {
                    _student.value = loadedStudent
                } else {
                    _error.value = "找不到學員"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "載入學員時發生錯誤"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateFirstName(firstName: String) {
        _student.value = _student.value.copy(firstName = firstName)
    }

    fun updateLastName(lastName: String) {
        _student.value = _student.value.copy(lastName = lastName)
    }

    fun updateAge(age: Int) {
        _student.value = _student.value.copy(age = age)
    }

    fun updateHeight(heightCm: Int) {
        _student.value = _student.value.copy(heightCm = heightCm)
    }

    fun updateNotes(notes: String) {
        _student.value = _student.value.copy(notes = notes)
    }

    fun saveStudent() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                studentRepository.upsertStudent(_student.value)
                _isSaved.value = true
                _error.value = null
            } catch (e: Exception) {
                _error.value = e.message ?: "儲存學員時發生錯誤"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
