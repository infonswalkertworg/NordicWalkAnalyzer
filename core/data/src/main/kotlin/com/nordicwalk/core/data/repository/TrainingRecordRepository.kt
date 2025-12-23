package com.nordicwalk.core.data.repository

import com.nordicwalk.core.domain.model.TrainingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface TrainingRecordRepository {
    suspend fun createRecord(record: TrainingRecord): Long
    suspend fun updateRecord(record: TrainingRecord)
    suspend fun deleteRecord(record: TrainingRecord)
    suspend fun getRecordById(id: Long): TrainingRecord?
    fun getRecordsByStudent(studentId: Long): Flow<List<TrainingRecord>>
    suspend fun getLatestRecords(studentId: Long, limit: Int = 10): List<TrainingRecord>
    suspend fun deleteByStudent(studentId: Long)
}
