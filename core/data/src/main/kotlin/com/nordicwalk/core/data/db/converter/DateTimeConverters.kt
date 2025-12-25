package com.nordicwalk.core.data.db.converter

import androidx.room.TypeConverter

/**
 * Room TypeConverter for date/time values
 * Uses Long timestamps (milliseconds since epoch) for simplicity and compatibility
 */
class DateTimeConverters {
    
    @TypeConverter
    fun fromTimestamp(value: Long?): Long? {
        return value
    }

    @TypeConverter
    fun dateToTimestamp(date: Long?): Long? {
        return date
    }
}
