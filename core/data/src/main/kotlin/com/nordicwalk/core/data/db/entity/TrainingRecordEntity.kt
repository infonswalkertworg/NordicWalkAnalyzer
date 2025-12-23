package com.nordicwalk.core.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "training_records",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("studentId")]
)
data class TrainingRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val studentId: Long,
    val date: Long, // Stored as System.currentTimeMillis() for date
    val time: String, // HH:mm format
    val distanceKm: Double?,
    val heartRateMax: Int?,
    val heartRateAvg: Int?,
    val vo2max: Double?,
    val description: String?,
    val screenshotUris: String?, // JSON array stored as string
    val improvementNotes: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)