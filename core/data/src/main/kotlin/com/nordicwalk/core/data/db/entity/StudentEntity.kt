package com.nordicwalk.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val contact: String?,
    val avatarUri: String?,
    val heightCm: Int,
    val poleLengthSuggested: Int,
    val poleLengthBeginner: Int,
    val poleLengthAdvanced: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)