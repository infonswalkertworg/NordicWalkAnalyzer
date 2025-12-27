package com.nordicwalk.feature.video.analysis.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "analysis_frames",
    foreignKeys = [
        ForeignKey(
            entity = AnalysisRecordEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AnalysisFrameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recordId: Long,
    val timestamp: Long,
    val framePath: String,
    val isKeyFrame: Boolean = false
)