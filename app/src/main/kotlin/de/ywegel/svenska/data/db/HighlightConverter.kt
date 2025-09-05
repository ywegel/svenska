package de.ywegel.svenska.data.db

import androidx.room.TypeConverter

class HighlightConverter {
    @TypeConverter
    fun fromHighlightRanges(ranges: List<Pair<Int, Int>>): String =
        ranges.joinToString(",") { "${it.first}:${it.second}" }

    @TypeConverter
    fun toHighlightRanges(value: String): List<Pair<Int, Int>> =
        if (value.isEmpty()) emptyList()
        else value.split(",").map {
            val (start, end) = it.split(":").map(String::toInt)
            start to end
        }
}