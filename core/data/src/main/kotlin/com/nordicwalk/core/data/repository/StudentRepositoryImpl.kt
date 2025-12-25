package com.nordicwalk.core.data.repository

import com.nordicwalk.core.data.db.dao.StudentDao
import com.nordicwalk.core.data.db.entity.StudentEntity
import com.nordicwalk.core.domain.model.Student
import com.nordicwalk.core.domain.model.PoleLengthCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StudentRepositoryImpl(
    private val studentDao: StudentDao
) : StudentRepository {

    override suspend fun upsertStudent(student: Student): Long {
        val (suggested, beginner, advanced) = if (student.heightCm > 0) {
            PoleLengthCalculator.calculatePoleLengths(student.heightCm)
        } else {
            Triple(student.poleLengthSuggested, student.poleLengthBeginner, student.poleLengthAdvanced)
        }

        val entity = StudentEntity(
            id = student.id,
            firstName = student.firstName,
            lastName = student.lastName,
            age = student.age,
            level = student.level,
            notes = student.notes,
            contact = student.contact,
            avatarUri = student.avatarUri,
            heightCm = student.heightCm,
            poleLengthSuggested = suggested,
            poleLengthBeginner = beginner,
            poleLengthAdvanced = advanced,
            createdAt = student.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        return studentDao.upsert(entity)
    }

    override suspend fun deleteStudent(studentId: Long) {
        studentDao.deleteById(studentId)
    }

    override suspend fun getStudentById(id: Long): Student? {
        return studentDao.getById(id)?.toDomainModel()
    }

    override fun getAllStudents(): Flow<List<Student>> {
        return studentDao.getAllStudents().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun searchStudents(query: String): Flow<List<Student>> {
        return studentDao.searchByName(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getStudentCount(): Int {
        return studentDao.getStudentCount()
    }

    private fun StudentEntity.toDomainModel(): Student = Student(
        id = id,
        firstName = firstName,
        lastName = lastName,
        age = age,
        level = level,
        notes = notes,
        contact = contact,
        avatarUri = avatarUri,
        heightCm = heightCm,
        poleLengthSuggested = poleLengthSuggested,
        poleLengthBeginner = poleLengthBeginner,
        poleLengthAdvanced = poleLengthAdvanced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
