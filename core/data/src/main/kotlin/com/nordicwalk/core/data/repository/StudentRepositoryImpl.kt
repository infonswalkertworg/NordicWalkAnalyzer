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

    override suspend fun createStudent(student: Student): Long {
        val (suggested, beginner, advanced) = PoleLengthCalculator.calculatePoleLengths(student.heightCm)
        val entity = StudentEntity(
            name = student.name,
            contact = student.contact,
            avatarUri = student.avatarUri,
            heightCm = student.heightCm,
            poleLengthSuggested = suggested,
            poleLengthBeginner = beginner,
            poleLengthAdvanced = advanced
        )
        return studentDao.insert(entity)
    }

    override suspend fun updateStudent(student: Student) {
        val (suggested, beginner, advanced) = PoleLengthCalculator.calculatePoleLengths(student.heightCm)
        val entity = StudentEntity(
            id = student.id,
            name = student.name,
            contact = student.contact,
            avatarUri = student.avatarUri,
            heightCm = student.heightCm,
            poleLengthSuggested = suggested,
            poleLengthBeginner = beginner,
            poleLengthAdvanced = advanced,
            createdAt = student.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        studentDao.update(entity)
    }

    override suspend fun deleteStudent(student: Student) {
        val entity = StudentEntity(
            id = student.id,
            name = student.name,
            contact = student.contact,
            avatarUri = student.avatarUri,
            heightCm = student.heightCm,
            poleLengthSuggested = student.poleLengthSuggested,
            poleLengthBeginner = student.poleLengthBeginner,
            poleLengthAdvanced = student.poleLengthAdvanced
        )
        studentDao.delete(entity)
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
        name = name,
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
