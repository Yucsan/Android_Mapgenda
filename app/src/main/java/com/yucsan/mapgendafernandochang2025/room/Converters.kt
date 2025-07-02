package com.yucsan.aventurafernandochang2025.room

import androidx.room.TypeConverter
import java.util.Date
import java.util.UUID

object Converters {

    @TypeConverter
    fun fromUUID(uuid: UUID?): String? = uuid?.toString()

    @TypeConverter
    fun toUUID(uuid: String?): UUID? = uuid?.let { UUID.fromString(it) }

    @TypeConverter
    fun fromLongToDate(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun fromDateToLong(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromListString(list: List<String>?): String? =
        list?.joinToString(separator = ",")

    @TypeConverter
    fun toListString(data: String?): List<String>? =
        data?.takeIf { it.isNotEmpty() }
            ?.split(",")
            ?.map { it.trim() }
}
