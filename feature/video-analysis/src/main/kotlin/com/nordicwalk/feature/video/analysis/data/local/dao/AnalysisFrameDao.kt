package com.nordicwalk.feature.video.analysis.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nordicwalk.feature.video.analysis.data.local.entity.AnalysisFrameEntity

@Dao
interface AnalysisFrameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFrame(frame: AnalysisFrameEntity)

    @Query("SELECT * FROM analysis_frames WHERE recordId = :recordId ORDER BY timestamp ASC")
    suspend fun getFramesByRecord(recordId: Long): List<AnalysisFrameEntity>
}