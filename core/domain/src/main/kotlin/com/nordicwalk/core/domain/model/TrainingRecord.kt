package com.nordicwalk.core.domain.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

data class TrainingRecord(
    val id: Long = 0L,
    val studentId: Long,
    val date: LocalDate,
    val time: LocalTime,
    val distanceKm: Double? = null,
    val heartRateMax: Int? = null,
    val heartRateAvg: Int? = null,
    val vo2max: Double? = null,
    val description: String? = null,
    val screenshotUris: List<String> = emptyList(),
    val improvementNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isValid: Boolean
        get() = studentId > 0

    val summary: String
        get() = buildString {
            append("$date $time")
            distanceKm?.let { append(" | ${it}km") }
            heartRateMax?.let { append(" | HR Max: ${it}bpm") }
            vo2max?.let { append(" | VO2: $it") }
        }
}