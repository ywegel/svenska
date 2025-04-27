package de.ywegel.svenska.data.db

import androidx.room.TypeConverter

class HighlightConverter {
    @TypeConverter
    fun fromString(value: String?): List<Int> {
        if (value.isNullOrEmpty()) return emptyList()
        return value.split(";").map { it.toInt() }
    }

    @TypeConverter
    fun toString(highlights: List<Int>): String {
        return highlights.joinToString(";")
    }
}
