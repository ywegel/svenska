package de.ywegel.svenska.data.model.quiz

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import de.ywegel.svenska.data.model.Vocabulary

// The FSRS state of one quizzed facet of a vocabulary.
// Only FacetCard cards save Fsrs. QuizPoolEntry is only used to manage a vocabularies pool state.
sealed interface FacetCard {
    val fsrs: FsrsColumns
}

data class FsrsColumns(
    val phase: CardPhase,
    val step: Int?,
    val stability: Double?,
    val difficulty: Double?,
    val due: Long,
    val lastReview: Long?,
)

// Learning progress for de->sv and sv->de translation Cards. Saves Vocabulary-Facet specific fields.
// introducedAt not needed. SV->DE is created with QuizPoolEntry and DE->SV has no daily limit.
@Entity(
    primaryKeys = ["vocabularyId", "direction"],
    foreignKeys = [
        ForeignKey(
            entity = Vocabulary::class,
            parentColumns = ["id"],
            childColumns = ["vocabularyId"],
            onDelete = CASCADE,
        ),
    ],
    indices = [Index("due")],
)
data class TranslationFacetCard(
    val vocabularyId: Int,
    val direction: QuizTranslationDirection,
    @Embedded override val fsrs: FsrsColumns,
) : FacetCard

// One forms card per changing word and its forms, shared by all rows with the same word and forms ("gå" with 14 meanings).
@Entity(indices = [Index("due")])
data class FormsFacetCard(
    @PrimaryKey val formsKey: String,
    @Embedded override val fsrs: FsrsColumns,
    val introducedAt: Long,
) : FacetCard

// The forms belong to the changing word only, so prefix and suffix are not part of the key.
val Vocabulary.formsKey: String?
    get() {
        if (form1 == null && form2 == null && form3 == null) return null
        return listOf(word, form1, form2, form3).joinToString("|") { it.orEmpty().trim().lowercase() }
    }
