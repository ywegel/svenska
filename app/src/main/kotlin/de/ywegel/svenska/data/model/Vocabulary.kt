package de.ywegel.svenska.data.model

import android.os.Parcelable
import androidx.compose.ui.text.AnnotatedString
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.ywegel.svenska.data.db.HighlightConverter
import de.ywegel.svenska.data.db.WordGroupConverter
import de.ywegel.svenska.ui.common.vocabulary.annotatedStringFromHighlights
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Parcelize
@Entity
@TypeConverters(HighlightConverter::class, WordGroupConverter::class)
data class Vocabulary(
    val word: String,
    val wordHighlights: List<Int> = emptyList(),
    val translation: String,
    // "ett" or "en"
    val gender: Gender? = null,
    // "Noun", "Verb", "Adjective" or "Other"
    val wordGroup: WordGroup = WordGroup.Other,
    val ending: String = "",
    val notes: String = "",
    val irregularPronunciation: String? = null,
    val isFavorite: Boolean = false,
    val containerId: Int,
    val lastEdited: Long = System.currentTimeMillis(),
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
) : Parcelable {

    @IgnoredOnParcel
    val annotatedWord: AnnotatedString by lazy {
        annotatedStringFromHighlights(word, wordHighlights)
    }

    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)

    val lastEditedDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(lastEdited)

    companion object {
        fun fromAnnotatedWord(
            wordWithHighlights: String,
            translation: String,
            gender: Gender?,
            wordGroup: WordGroup,
            ending: String,
            notes: String,
            containerId: Int,
            lastEdited: Long = System.currentTimeMillis(),
            created: Long = System.currentTimeMillis(),
        ): Vocabulary {
            val (annotations, cleanString) = extractAnnotations(wordWithHighlights)

            return Vocabulary(
                word = cleanString,
                wordHighlights = annotations,
                translation = translation,
                gender = gender,
                wordGroup = wordGroup,
                ending = ending,
                notes = notes,
                containerId = containerId,
                lastEdited = lastEdited,
                created = created,
            )
        }
    }
}

// TODO: if highlights are just one apart (a**b), remove the highlight
fun extractAnnotations(wordWithHighlights: String): Pair<List<Int>, String> {
    var annotationCount = 0
    val annotations = mutableListOf<Int>()
    val cleanString = StringBuilder(wordWithHighlights.length)

    wordWithHighlights.forEachIndexed { index, c ->
        if (c == '*') {
            annotations.add(index - annotationCount)
            annotationCount++
        } else {
            cleanString.append(c)
        }
    }

    return Pair(annotations, cleanString.toString())
}

enum class Gender {
    Ultra, // en
    Neutra, // ett
    ;

    companion object {
        val defaultIfEmpty = Ultra
    }
}
