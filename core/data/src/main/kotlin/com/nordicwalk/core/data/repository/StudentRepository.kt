package com.nordicwalk.core.data.repository

import com.nordicwalk.core.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    suspend fun upsertStudent(student: Student): Long
    suspend fun deleteStudent(studentId: Long)
    suspend fun getStudentById(id: Long): Student?
    fun getAllStudents(): Flow<List<Student>>
    suspend fun searchStudents(query: String): Flow<List<Student>>
    suspend fun getStudentCount(): Int
}
