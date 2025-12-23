package com.nordicwalk.core.data.repository

import com.nordicwalk.core.data.db.dao.TrainingRecordDao
import com.nordicwalk.core.data.db.entity.TrainingRecordEntity
import com.nordicwalk.core.domain.model.TrainingRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Instant

class TrainingRecordRepositoryImpl(
    private val trainingRecordDao: TrainingRecordDao
) : TrainingRecordRepository {

    override suspend fun createRecord(record: TrainingRecord): Long {
        val entity = record.toEntity()
        return trainingRecordDao.insert(entity)
    }

    override suspend fun updateRecord(record: TrainingRecord) {
        val entity = record.toEntity().copy(
            updatedAt = System.currentTimeMillis()
        )
        trainingRecordDao.update(entity)
    }

    override suspend fun deleteRecord(record: TrainingRecord) {
        val entity = record.toEntity()
        trainingRecordDao.delete(entity)
    }

    override suspend fun getRecordById(id: Long): TrainingRecord? {
        return trainingRecordDao.getById(id)?.toDomainModel()
    }

    override fun getRecordsByStudent(studentId: Long): Flow<List<TrainingRecord>> {
        return trainingRecordDao.getRecordsByStudent(studentId).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getLatestRecords(studentId: Long, limit: Int): List<TrainingRecord> {
        return trainingRecordDao.getLatestRecords(studentId, limit).map { it.toDomainModel() }
    }

    override suspend fun deleteByStudent(studentId: Long) {
        trainingRecordDao.deleteByStudent(studentId)
    }

    private fun TrainingRecord.toEntity(): TrainingRecordEntity {
        val dateTimestamp = Instant.parse("${date}T${time}:00Z").toEpochMilliseconds()
        val screenshotJson = if (screenshotUris.isNotEmpty()) {
            screenshotUris.joinToString(",", "[\"", "\"]")
        } else null

        return TrainingRecordEntity(
            id = id,
            studentId = studentId,
            date = dateTimestamp,
            time = time.toString(),
            distanceKm = distanceKm,
            heartRateMax = heartRateMax,
            heartRateAvg = heartRateAvg,
            vo2max = vo2max,
            description = description,
            screenshotUris = screenshotJson,
            improvementNotes = improvementNotes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun TrainingRecordEntity.toDomainModel(): TrainingRecord {
        val localDate = Instant.fromEpochMilliseconds(date).toString().split("T")[0]
            .let { LocalDate.parse(it) }
        val localTime = LocalTime.parse(time)

        val screenshots = try {
            screenshotUris?.removeSurrounding("[\"", "\"]")
                ?.split("\",\"")
                ?.filter { it.isNotBlank() }
                ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        return TrainingRecord(
            id = id,
            studentId = studentId,
            date = localDate,
            time = localTime,
            distanceKm = distanceKm,
            heartRateMax = heartRateMax,
            heartRateAvg = heartRateAvg,
            vo2max = vo2max,
            description = description,
            screenshotUris = screenshots,
            improvementNotes = improvementNotes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}
