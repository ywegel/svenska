package de.ywegel.svenska.data.db.quiz

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.QuizPoolStatus
import de.ywegel.svenska.data.model.quiz.formsKey

/**
 * The FSRS state of one quizzed facet ([QuizFacet]) of a vocabulary. Each facet has its own state, due date and
 * learning steps.
 *
 * Only facet cards hold FSRS values. [QuizPoolEntry] only manages whether and since when a vocabulary is in the quiz
 * pool.
 *
 * A facet card exists only once its facet is unlocked. A missing card (missing == not in the database)
 * means the facet is still locked. When a card is created, it starts as a new FSRS card: [CardPhase.LEARNING] at step 0, without
 * stability, difficulty and last review. Both card tables hold only the key and the FSRS state of a card.
 *
 */
sealed interface FacetCard {
    val fsrs: FsrsColumns
}

/**
 * The phase of a [FacetCard] in the FSRS scheduler, stored by name.
 *
 * A copy of FsrsKt's `FsrsCardState`, so that a renamed constant in the library can't make stored cards unreadable.
 *
 * - [LEARNING]: new, or going through the learning steps (minutes).
 * - [REVIEW]: scheduled in whole days from the desired retention.
 * - [RELEARNING]: forgotten in review, going through the relearning steps.
 */
enum class CardPhase { LEARNING, REVIEW, RELEARNING }

/**
 * The FSRS columns that both card tables share, embedded with their plain column names (`due`, `step`, …).
 * Mapped to and from FsrsKt's `FsrsCard` in `data/impl/quiz/FsrsMapping.kt`.
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
) {
    companion object {
        /**
         * The columns of a newly created card, due at [due].
         */
        fun newCard(due: Long) = FsrsColumns(
            phase = CardPhase.LEARNING,
            step = 0,
            stability = null,
            difficulty = null,
            due = due,
            lastReview = null,
        )
    }
}

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
 */
@Entity(indices = [Index("due")])
data class FormsFacetCard(
    @PrimaryKey val formsKey: String,
    @Embedded override val fsrs: FsrsColumns,
) : FacetCard
