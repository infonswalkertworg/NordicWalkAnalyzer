package com.nordicwalk.core.domain.model

data class Student(
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
    val isValid: Boolean
        get() = firstName.isNotBlank() && lastName.isNotBlank() && age > 0

    // For backward compatibility
    val name: String
        get() = "$firstName $lastName".trim()

    companion object {
        fun empty() = Student(
            id = 0L,
            firstName = "",
            lastName = "",
            age = 0,
            level = "",
            notes = ""
        )
    }
}

object PoleLengthCalculator {
    /**
     * 計算北歐式健走杖建議長度
     * 公式：身高 * 0.68，然後往下取至下一個5公分
     * @param heightCm 學員身高（公分）
     * @return Triple(建議長度, 初階長度, 進階長度)
     */
    fun calculatePoleLengths(heightCm: Int): Triple<Int, Int, Int> {
        val raw = (heightCm * 0.68).toInt()
        val suggested = (raw / 5) * 5
        val beginner = suggested - 5
        val advanced = suggested + 5
        return Triple(suggested, beginner, advanced)
    }
}
