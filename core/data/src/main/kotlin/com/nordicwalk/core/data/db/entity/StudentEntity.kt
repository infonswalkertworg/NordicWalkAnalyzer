package com.nordicwalk.core.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class StudentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val firstName: String = "",
    val lastName: String = "",
    val age: Int = 0,
    val level: String = "",
    val notes: String = "",
    val contact: String? = null,
    val avatarUri: String? = null,
    val heightCm: Int = 0,
    val poleLengthSuggested: Int = 0,
    val poleLengthBeginner: Int = 0,
    val poleLengthAdvanced: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    // Computed property for backward compatibility
    val name: String
        get() = "$firstName $lastName".trim()
}
