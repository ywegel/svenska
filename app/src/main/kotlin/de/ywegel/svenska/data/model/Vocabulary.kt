package de.ywegel.svenska.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import de.ywegel.svenska.data.db.HighlightConverter
import de.ywegel.svenska.data.db.WordGroupConverter
import kotlinx.parcelize.Parcelize
import java.text.DateFormat

@Parcelize
@Entity
@TypeConverters(HighlightConverter::class, WordGroupConverter::class)
data class Vocabulary(
    // Only the word the endings belong to: "dröm" in "högsta dröm (-men)". The full text is prefix + word + suffix
    val word: String,
    // Text before the changing word, the same in all forms: "högsta" in "högsta dröm (-men)"
    @ColumnInfo(defaultValue = "") val wordPrefix: String = "",
    // Text after the changing word, the same in all forms: "sig" in "känna sig (-er, kände, känt)"
    @ColumnInfo(defaultValue = "") val wordSuffix: String = "",
    val wordHighlights: List<Pair<Int, Int>> = emptyList(),
    val translation: String,
    // "ett" or "en"
    val gender: Gender? = null,
    // "Noun", "Verb", "Adjective" or "Other"
    val wordGroup: WordGroup = WordGroup.Other,
    val ending: String = "",
    // Written-out forms of `word` only, without prefix and suffix. Variants stay text ("lade/la"). null = no form.
    // Noun: sg. definite · pl. indefinite · pl. definite
    // Verb: present · past · supine
    // Adjective: neuter · plural, or comparative · superlative
    val form1: String? = null,
    val form2: String? = null,
    val form3: String? = null,
    // Set by the importer when the forms don't fit together ("klass (-en -er -arna)"). Cleared once the user fixes or confirms them
    @ColumnInfo(defaultValue = "0") val formsNeedReview: Boolean = false,
    val notes: String = "",
    val irregularPronunciation: String? = null,
    val isFavorite: Boolean = false,
    val containerId: Int,
    val lastEdited: Long = System.currentTimeMillis(),
    val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
) : Parcelable {

    val createdDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(created)

    val lastEditedDateFormatted: String
        get() = DateFormat.getDateTimeInstance().format(lastEdited)

    val hasForms: Boolean
        get() = form1 != null || form2 != null || form3 != null
}

enum class Gender {
    Ultra, // en
    Neutra, // ett
    ;

    companion object {
        val defaultIfEmpty = Ultra
    }
}
