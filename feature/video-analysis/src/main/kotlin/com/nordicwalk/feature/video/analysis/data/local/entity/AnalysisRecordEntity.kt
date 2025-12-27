package com.nordicwalk.feature.video.analysis.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "analysis_records")
data class AnalysisRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val studentId: Long,
    val analysisDate: String, // yyyy-MM-dd HH:mm:ss
    val sequenceNumber: String,
    val recordCode: String,
    val videoPath: String,
    val thumbnailPath: String?,
    val analysisType: String,
    val duration: Long,
    val overallScore: Float = 0f,
    val createdAt: Long = System.currentTimeMillis()
)