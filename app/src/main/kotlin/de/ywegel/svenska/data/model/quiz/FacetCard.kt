package de.ywegel.svenska.data.model.quiz

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import de.ywegel.svenska.data.model.Vocabulary

/**
 * The FSRS state of one quizzed facet ([QuizFacet]) of a vocabulary. Each facet has its own state, due date and
 * learning steps.
 *
 * Only facet cards hold FSRS values. [QuizPoolEntry] only manages whether and since when a vocabulary is in the quiz
 * pool.
 *
 * A facet card exists only once its facet is unlocked. A missing card (missing == not in the database)
 * means the facet is still locked. When a card is created, it starts as a new FSRS card: [CardPhase.LEARNING] at step 0, without
 * stability, difficulty and last review.
 */
sealed interface FacetCard {
    val fsrs: FsrsColumns
}

/**
 * The FSRS columns that both card tables share, embedded with their plain column names (`due`, `step`, …).
 * Mapped to and from FsrsKt's `FsrsCard` in `FsrsMapping.kt`.
 *
 * @property phase The phase of the card in the FSRS scheduler.
 * @property step The current learning or relearning step, as an index into the scheduler's steps. `null` in
 * [CardPhase.REVIEW].
 * @property stability The FSRS stability in days, `null` before the first review.
 * @property difficulty The FSRS difficulty from 1 to 10, `null` before the first review.
 * @property due When the card is due, in epoch milliseconds. Only due cards are quizzed.
 * @property lastReview When the card was reviewed last, in epoch milliseconds, `null` before the first review.
 */
data class FsrsColumns(
    val phase: CardPhase,
    val step: Int?,
    val stability: Double?,
    val difficulty: Double?,
    val due: Long,
    val lastReview: Long?,
)

/**
 * The card of one translation direction of a vocabulary row.
 *
 * - [QuizTranslationDirection.SV_DE] is created when the word is introduced, see [QuizPoolEntry.introducedAt]. It
 *   counts towards the daily limit of new cards only through that introduction.
 * - [QuizTranslationDirection.DE_SV] is created after the first GOOD or EASY on SV_DE, due at the start of the next
 *   day (4 AM local time). It has no daily introduction limit.
 *
 * Resetting the word (see [QuizPoolEntry.resetAt]) turns the SV_DE card back into a new card and deletes the DE_SV
 * card, so that DE_SV has to be unlocked again.
 */
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

/**
 * The forms card of a changing word and its forms, shared by all vocabulary rows with the same [Vocabulary.formsKey].
 * A word with X different entries for different translations/meanings has X database entries, but only one forms card.
 *
 * The card has no foreign key, as no single row owns it. It is created when its introduction card is shown, which
 * requires that one of its rows has an [QuizPoolEntry.svDeStreak] of at least 3, has forms and does not have
 * `formsNeedReview` set. Whether a word is ready for its forms is derived from these values, not stored.
 *
 * The card is quizzed as long as at least one of its rows is [QuizPoolStatus.ACTIVE]. A card whose word no longer
 * exists in any row stays in the table and is never quizzed. If a word with the same forms is added again, its
 * progress continues.
 *
 * Changing the forms of a row changes its [Vocabulary.formsKey], so the row then points to a new, locked forms card.
 * To keep the progress, the repository renames the key of this card and of its logs, but only if no other row still
 * uses the old key.
 *
 * @property formsKey The key from [Vocabulary.formsKey].
 * @property introducedAt When the card was created, in epoch milliseconds. A new forms card counts towards the same
 * daily limit of new cards as a newly introduced word ([QuizPoolEntry.introducedAt]).
 */
@Entity(indices = [Index("due")])
data class FormsFacetCard(
    @PrimaryKey val formsKey: String,
    @Embedded override val fsrs: FsrsColumns,
    val introducedAt: Long,
) : FacetCard

/**
 * The key of the forms card of this row, or `null` if the row has no forms, so it has no forms card.
 *
 * Made of the changing [Vocabulary.word] and its three forms, trimmed and in lowercase. The prefix and suffix are not
 * part of it, as the forms belong to the changing word only: `känna sig` and `känna` share their forms card.
 */
val Vocabulary.formsKey: String?
    get() {
        if (form1 == null && form2 == null && form3 == null) return null
        return listOf(word, form1, form2, form3).joinToString("|") { it.orEmpty().trim().lowercase() }
    }
