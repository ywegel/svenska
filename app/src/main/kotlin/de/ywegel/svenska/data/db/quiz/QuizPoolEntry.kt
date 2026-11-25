package de.ywegel.svenska.data.db.quiz

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizPoolStatus
import de.ywegel.svenska.data.model.quiz.QuizTranslationDirection

/**
 * The quiz pool state of one vocabulary row. It holds no FSRS values; those live in the [FacetCard]s of the row.
 *
 * A row without entry is not in the pool. Adding it to the pool creates the entry as [QuizPoolStatus.QUEUED].
 * Removing a queued word deletes the entry, removing an active word pauses it. The [poolStatus] is derived from the
 * fields, not stored.
 *
 * @property queuedAt When the word was added to the pool, in epoch milliseconds. Queued words are introduced first in,
 * first out. Words added together, e.g. a whole container, have the same time and are introduced in the order of
 * their [vocabularyId].
 * @property introducedAt When the word was introduced, in epoch milliseconds, `null` while it is queued. Counts towards
 * the daily limit of new cards, together with [formsIntroducedAt]. Introducing the word creates its
 * [QuizTranslationDirection.SV_DE] card. Pausing and resuming keeps it, so a resumed word doesn't count again.
 * @property formsIntroducedAt When this row introduced the shared [FormsFacetCard] of its forms, in epoch
 * milliseconds, `null` if it didn't. Set when the introduction card of the forms is shown and the forms card is
 * created. If another row with the same forms introduced the card first, it stays `null`, so a shared card counts
 * only once towards the daily limit of new cards. A reset of the row keeps it, as the shared card is not reset. It
 * only counts new cards, it doesn't lock the row: After an edit of the forms, the row can introduce the card of its new
 * forms, which sets it again.
 * @property isPaused Whether the word was removed from the pool after its introduction. Its cards are not quizzed, but
 * keep their progress. Only an introduced word can be paused.
 * @property svDeStreak The number of SV_DE reviews in a row with a final rating of GOOD or EASY since [resetAt]. Any
 * other final rating sets it back to 0. Stored instead of derived from the logs, as the forms unlock needs it for all
 * active words at once. Updated in the same transaction as the log, and again when "War doch richtig" changes the
 * final rating.
 * @property resetAt When the progress of the word was reset last, in epoch milliseconds, `null` if never. A reset sets
 * the [svDeStreak] to 0, makes the SV_DE card new and deletes the DE_SV card. Logs before this time are kept, but no
 * longer count when the cards are recomputed from the logs. The forms card is not reset, as it is shared.
 */
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Vocabulary::class,
            parentColumns = ["id"],
            childColumns = ["vocabularyId"],
            onDelete = CASCADE,
        ),
    ],
)
data class QuizPoolEntry(
    @PrimaryKey val vocabularyId: Int,
    val queuedAt: Long,
    val introducedAt: Long? = null,
    val formsIntroducedAt: Long? = null,
    val isPaused: Boolean = false,
    val svDeStreak: Int = 0,
    val resetAt: Long? = null,
) {
    init {
        require(!isPaused || introducedAt != null) { "Only an introduced word can be paused, but was $this." }
        require(formsIntroducedAt == null || introducedAt != null) {
            "Only an introduced word can introduce its forms, but was $this."
        }
    }

    val poolStatus: QuizPoolStatus
        get() = when {
            isPaused -> QuizPoolStatus.PAUSED
            introducedAt == null -> QuizPoolStatus.QUEUED
            else -> QuizPoolStatus.ACTIVE
        }

    fun introduce(now: Long): QuizPoolEntry {
        check(poolStatus == QuizPoolStatus.QUEUED) { "Only a queued word can be introduced, but was $this." }
        return copy(introducedAt = now)
    }

    fun pause(): QuizPoolEntry = copy(isPaused = true)   // init catches a queued word

    fun resume(): QuizPoolEntry = copy(isPaused = false)

    fun reset(now: Long): QuizPoolEntry = copy(svDeStreak = 0, resetAt = now)
}
