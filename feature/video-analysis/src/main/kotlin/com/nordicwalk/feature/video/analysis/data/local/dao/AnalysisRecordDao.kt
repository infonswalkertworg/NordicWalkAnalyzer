package com.nordicwalk.feature.video.analysis.data.local.dao

import androidx.room.*
import com.nordicwalk.feature.video.analysis.data.local.entity.AnalysisRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalysisRecordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalysisRecord(record: AnalysisRecordEntity): Long

    @Query("SELECT * FROM analysis_records WHERE studentId = :studentId ORDER BY id DESC")
    fun getRecordsByStudent(studentId: Long): Flow<List<AnalysisRecordEntity>>

    @Query("SELECT * FROM analysis_records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Long): AnalysisRecordEntity?

    @Query("SELECT COUNT(*) FROM analysis_records WHERE studentId = :studentId AND substr(analysisDate, 1, 10) = :todayDate")
    suspend fun getTodayCount(studentId: Long, todayDate: String): Int
}