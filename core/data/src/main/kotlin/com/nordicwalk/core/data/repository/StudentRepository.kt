package com.nordicwalk.core.data.repository

import com.nordicwalk.core.domain.model.Student
import kotlinx.coroutines.flow.Flow

interface StudentRepository {
    suspend fun createStudent(student: Student): Long
    suspend fun updateStudent(student: Student)
    suspend fun deleteStudent(student: Student)
    suspend fun getStudentById(id: Long): Student?
    fun getAllStudents(): Flow<List<Student>>
    suspend fun searchStudents(query: String): Flow<List<Student>>
    suspend fun getStudentCount(): Int
}
