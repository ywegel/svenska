package de.ywegel.svenska.data.model

import android.os.Parcelable
import androidx.compose.ui.text.AnnotatedString
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.ywegel.svenska.data.db.HighlightConverter
import de.ywegel.svenska.data.db.WordGroupConverter
import de.ywegel.svenska.ui.common.vocabulary.HighlightUtils
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Parcelize
@Entity
@TypeConverters(HighlightConverter::class, WordGroupConverter::class)
data class Vocabulary(
    val word: String,
    val wordHighlights: List<Pair<Int, Int>> = emptyList(),
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
        HighlightUtils.buildAnnotatedWord(word, wordHighlights)
    }

    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)

    val lastEditedDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(lastEdited)
}

enum class Gender {
    Ultra, // en
    Neutra, // ett
    ;

    companion object {
        val defaultIfEmpty = Ultra
    }
}
