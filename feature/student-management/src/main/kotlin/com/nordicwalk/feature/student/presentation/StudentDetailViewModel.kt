package com.nordicwalk.feature.student.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nordicwalk.core.data.repository.StudentRepository
import com.nordicwalk.core.domain.model.Student
import com.nordicwalk.feature.video.analysis.data.local.dao.AnalysisRecordDao // ✅ 新增 Import
import com.nordicwalk.feature.video.analysis.data.local.entity.AnalysisRecordEntity // ✅ 新增 Import
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val studentRepository: StudentRepository,
    private val analysisRecordDao: AnalysisRecordDao // ✅ 新增注入 DAO
) : ViewModel() {

    private val studentId: Long = savedStateHandle["studentId"] ?: 0L

    private val _student = MutableStateFlow<Student?>(null)
    val student: StateFlow<Student?> = _student.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // ✅ 新增：即時監聽資料庫中的分析紀錄
    // 使用 stateIn 將 Flow 轉換為 StateFlow，這樣 UI 可以直接 collectAsState
    val analysisRecords: StateFlow<List<AnalysisRecordEntity>> = analysisRecordDao.getRecordsByStudent(studentId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000), // 暫停訂閱 5 秒後停止，節省資源
            initialValue = emptyList()
        )

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
