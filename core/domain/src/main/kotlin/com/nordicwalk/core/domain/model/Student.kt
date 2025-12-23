package com.nordicwalk.core.domain.model

data class Student(
    val id: Long = 0L,
    val name: String,
    val contact: String? = null,
    val avatarUri: String? = null,
    val heightCm: Int,
    val poleLengthSuggested: Int,
    val poleLengthBeginner: Int,
    val poleLengthAdvanced: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isValid: Boolean
        get() = name.isNotBlank() && heightCm > 0
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