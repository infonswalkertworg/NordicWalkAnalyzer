package com.nordicwalk.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nordicwalk.core.data.db.entity.TrainingRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TrainingRecordEntity): Long

    @Update
    suspend fun update(record: TrainingRecordEntity)

    @Delete
    suspend fun delete(record: TrainingRecordEntity)

    @Query("SELECT * FROM training_records WHERE id = :id")
    suspend fun getById(id: Long): TrainingRecordEntity?

    @Query("SELECT * FROM training_records WHERE studentId = :studentId ORDER BY date DESC")
    fun getRecordsByStudent(studentId: Long): Flow<List<TrainingRecordEntity>>

    @Query("SELECT * FROM training_records WHERE studentId = :studentId ORDER BY date DESC LIMIT :limit")
    suspend fun getLatestRecords(studentId: Long, limit: Int = 10): List<TrainingRecordEntity>

    @Query("SELECT * FROM training_records WHERE studentId = :studentId AND date >= :fromDate ORDER BY date DESC")
    suspend fun getRecordsByDateRange(studentId: Long, fromDate: Long): List<TrainingRecordEntity>

    @Query("DELETE FROM training_records WHERE studentId = :studentId")
    suspend fun deleteByStudent(studentId: Long)

    @Query("SELECT COUNT(*) FROM training_records WHERE studentId = :studentId")
    suspend fun getRecordCount(studentId: Long): Int
}