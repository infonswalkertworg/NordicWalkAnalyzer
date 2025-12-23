package com.nordicwalk.feature.student.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.core.data.repository.StudentRepository
import com.nordicwalk.core.domain.model.Student
import com.nordicwalk.core.domain.model.PoleLengthCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentFormUiState(
    val id: Long = 0L,
    val name: String = "",
    val contact: String = "",
    val avatarUri: String? = null,
    val heightCm: String = "",
    val suggestedPoleLength: Int? = null,
    val beginnerPoleLength: Int? = null,
    val advancedPoleLength: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSaved: Boolean = false,
    val nameError: String? = null,
    val heightError: String? = null
)

@HiltViewModel
class StudentFormViewModel @Inject constructor(
    private val studentRepository: StudentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val studentId: Long = savedStateHandle["studentId"] ?: 0L

    private val _uiState = MutableStateFlow(StudentFormUiState())
    val uiState: StateFlow<StudentFormUiState> = _uiState.asStateFlow()

    init {
        if (studentId > 0) {
            loadStudent(studentId)
        }
    }

    private fun loadStudent(id: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val student = studentRepository.getStudentById(id)
                if (student != null) {
                    _uiState.update {
                        it.copy(
                            id = student.id,
                            name = student.name,
                            contact = student.contact ?: "",
                            avatarUri = student.avatarUri,
                            heightCm = student.heightCm.toString(),
                            suggestedPoleLength = student.poleLengthSuggested,
                            beginnerPoleLength = student.poleLengthBeginner,
                            advancedPoleLength = student.poleLengthAdvanced,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "學員不存在") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update {
            it.copy(
                name = name,
                nameError = if (name.isBlank()) "姓名不能為空" else null
            )
        }
    }

    fun updateContact(contact: String) {
        _uiState.update { it.copy(contact = contact) }
    }

    fun updateHeight(height: String) {
        _uiState.update {
            val heightError = if (height.isNotBlank()) {
                val cm = height.toIntOrNull()
                when {
                    cm == null -> "請輸入有效的身高"
                    cm < 100 || cm > 250 -> "身高應在100-250公分之間"
                    else -> null
                }
            } else {
                "身高不能為空"
            }

            val state = it.copy(heightCm = height, heightError = heightError)

            // 自動計算健走杖長度
            if (heightError == null && height.isNotBlank()) {
                val cm = height.toIntOrNull() ?: return@update state
                val (suggested, beginner, advanced) = PoleLengthCalculator.calculatePoleLengths(cm)
                state.copy(
                    suggestedPoleLength = suggested,
                    beginnerPoleLength = beginner,
                    advancedPoleLength = advanced
                )
            } else {
                state
            }
        }
    }

    fun updateAvatarUri(uri: String) {
        _uiState.update { it.copy(avatarUri = uri) }
    }

    fun saveStudent() {
        val state = _uiState.value

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "姓名不能為空") }
            return
        }

        val heightCm = state.heightCm.toIntOrNull()
        if (heightCm == null || heightCm < 100 || heightCm > 250) {
            _uiState.update { it.copy(heightError = "身高無效") }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val (suggested, beginner, advanced) = PoleLengthCalculator.calculatePoleLengths(heightCm)

                val student = Student(
                    id = state.id,
                    name = state.name,
                    contact = state.contact.ifBlank { null },
                    avatarUri = state.avatarUri,
                    heightCm = heightCm,
                    poleLengthSuggested = suggested,
                    poleLengthBeginner = beginner,
                    poleLengthAdvanced = advanced
                )

                if (state.id > 0) {
                    studentRepository.updateStudent(student)
                } else {
                    studentRepository.createStudent(student)
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
