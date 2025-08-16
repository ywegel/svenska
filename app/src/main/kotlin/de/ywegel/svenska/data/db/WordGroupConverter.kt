package de.ywegel.svenska.data.db

import androidx.room.TypeConverter
import de.ywegel.svenska.data.model.WordGroup

class WordGroupConverter {
    @TypeConverter
    fun toString(wordGroup: WordGroup): String = when (wordGroup) {
        is WordGroup.Verb -> "VERB:${wordGroup.subgroup.name}"
        is WordGroup.Noun -> "NOUN:${wordGroup.subgroup.name}"
        WordGroup.Adjective -> "ADJECTIVE"
        WordGroup.Other -> "OTHER"
    }

    @TypeConverter
    fun fromString(value: String): WordGroup {
        val parts = value.split(":")
        return when (parts[0]) {
            "VERB" -> WordGroup.Verb(
                parts.getOrNull(1)?.let { WordGroup.VerbSubgroup.valueOf(it) }
                    ?: WordGroup.VerbSubgroup.UNDEFINED,
            )

            "NOUN" -> WordGroup.Noun(
                parts.getOrNull(1)?.let { WordGroup.NounSubgroup.valueOf(it) }
                    ?: WordGroup.NounSubgroup.UNDEFINED,
            )

            "ADJECTIVE" -> WordGroup.Adjective
            else -> WordGroup.Other
        }
    }
}
