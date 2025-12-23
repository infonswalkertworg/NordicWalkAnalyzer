package com.nordicwalk.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "analysis_sessions",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("studentId")]
)
data class AnalysisSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val studentId: Long?,
    val source: String, // CAMERA or IMPORTED_VIDEO
    val videoUri: String?,
    val direction: String, // FRONT, BACK, LEFT, RIGHT
    val actualDirection: String?,
    val metricsJson: String?, // Serialized metrics summary
    val reportText: String?,
    val thumbnailUri: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)