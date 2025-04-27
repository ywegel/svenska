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
            "VERB" -> WordGroup.Verb(WordGroup.VerbSubgroup.valueOf(parts[1]))
            "NOUN" -> WordGroup.Noun(WordGroup.NounSubgroup.valueOf(parts[1]))
            "ADJECTIVE" -> WordGroup.Adjective
            else -> WordGroup.Other
        }
    }
}
