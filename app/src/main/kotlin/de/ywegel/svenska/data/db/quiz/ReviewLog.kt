package de.ywegel.svenska.data.db.quiz

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.Index
import androidx.room.PrimaryKey
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizFacet

/**
 * One review of a facet card. Append-only: rows are only changed to move them to another row or key (merging
 * duplicate rows, renaming a forms key), never to change what happened.
 *
 * The cards can be recomputed from their logs with FsrsKt's `FsrsScheduler.reschedule`, e.g. after a change of the
 * parameters or a fix in the FsrsKt lib, and the FSRS optimizer can compute personal parameters from them. The logs of a
 * translation card are those with its [vocabularyId] and [facet] after [QuizPoolEntry.resetAt], the logs of a forms
 * card are those with its [formsKey], ordered by [reviewAt] and then [id].
 *
 * Reviews in the free practice mode don't change FSRS and are not logged.
 *
 * @property vocabularyId The row that was quizzed. Deleting the row deletes its logs. Before deleting a duplicate row
 * whose logs should be kept, move them to the remaining row.
 * @property formsKey The key of the forms card at the time of the review, set exactly for [QuizFacet.FORMS]. Stored,
 * as the forms of the row can change later.
 * @property rating The final rating, 1 (AGAIN) to 4 (EASY), after "War doch richtig" or "Leicht". These are the FSRS
 * standard values that py-fsrs and the FSRS optimizer use as well.
 * @property rawAnswer What was typed. For [QuizFacet.FORMS] all answered parts in the order they were asked, separated
 * by `|`.
 * @property reviewAt When the review happened, in epoch milliseconds.
 * @property durationMillis How long the answer took, `null` if unknown.
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
    indices = [Index("vocabularyId", "facet"), Index("formsKey")],
)
data class ReviewLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vocabularyId: Int,
    val facet: QuizFacet,
    val formsKey: String?,
    val rating: Int,
    val rawAnswer: String,
    val reviewAt: Long,
    val durationMillis: Long?,
) {
    init {
        require((facet == QuizFacet.FORMS) == (formsKey != null)) {
            "formsKey must be set exactly for FORMS, but was $this."
        }
        require(rating in 1..HIGHEST_RATING) {
            "rating must be between 1 and $HIGHEST_RATING, but was $rating."
        }
    }
}

// EASY, the highest FSRS rating
private const val HIGHEST_RATING = 4
