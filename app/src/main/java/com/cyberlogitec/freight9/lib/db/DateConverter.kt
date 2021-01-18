package com.cyberlogitec.freight9.lib.db

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        var date:Date? = if (timestamp == null) null else Date(timestamp)
        return date
    }

    @TypeConverter
    fun toTimestamp(date: Date?): Long? {
        val time:Long? = date?.time
        return time
    }
}
