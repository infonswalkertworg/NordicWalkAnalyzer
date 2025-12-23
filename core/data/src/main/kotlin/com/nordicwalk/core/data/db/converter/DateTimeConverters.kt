package com.nordicwalk.core.data.db.converter

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.Instant

class DateTimeConverters {
    @TypeConverter
    fun fromTimestampToLocalDate(timestamp: Long): LocalDate? = try {
        Instant.fromEpochMilliseconds(timestamp).toString().split("T")[0].let {
            LocalDate.parse(it)
        }
    } catch (e: Exception) {
        null
    }

    @TypeConverter
    fun localDateToTimestamp(date: LocalDate?): Long? =
        date?.let { Instant.parse("${it}T00:00:00Z").toEpochMilliseconds() }

    @TypeConverter
    fun fromLocalTimeString(value: String?): LocalTime? = value?.let {
        try {
            LocalTime.parse(it)
        } catch (e: Exception) {
            null
        }
    }

    @TypeConverter
    fun localTimeToString(time: LocalTime?): String? = time?.toString()
}