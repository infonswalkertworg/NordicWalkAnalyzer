package com.nordicwalk.core.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.nordicwalk.core.data.db.entity.AnalysisSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: AnalysisSessionEntity): Long

    @Update
    suspend fun update(session: AnalysisSessionEntity)

    @Delete
    suspend fun delete(session: AnalysisSessionEntity)

    @Query("SELECT * FROM analysis_sessions WHERE id = :id")
    suspend fun getById(id: Long): AnalysisSessionEntity?

    @Query("SELECT * FROM analysis_sessions WHERE studentId = :studentId ORDER BY createdAt DESC")
    fun getSessionsByStudent(studentId: Long): Flow<List<AnalysisSessionEntity>>

    @Query("SELECT * FROM analysis_sessions ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getLatestSessions(limit: Int = 20): List<AnalysisSessionEntity>

    @Query("DELETE FROM analysis_sessions WHERE studentId = :studentId")
    suspend fun deleteByStudent(studentId: Long)

    @Query("DELETE FROM analysis_sessions WHERE createdAt < :olderThanMillis")
    suspend fun deleteOldSessions(olderThanMillis: Long)

    @Query("SELECT COUNT(*) FROM analysis_sessions WHERE studentId = :studentId")
    suspend fun getSessionCount(studentId: Long): Int
}