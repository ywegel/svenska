package de.ywegel.svenska.data.model.quiz

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey
import de.ywegel.svenska.data.model.Vocabulary

// Learning progress of one Vocabulary Entry. Saves Vocabulary specific fields. Learning Progress does not save Fsrs values, as Fsrs is only applied to the Vocabularies Facets.
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
    val queuedAt: Long,        // needed for comparison for FIFO of QuizPool
    val introducedAt: Long? = null,    // needed to limit daily new words
    val isPaused: Boolean = false,
    val svDeStreak: Int = 0,
    val resetAt: Long? = null,
) {
    init {
        require(!isPaused || introducedAt != null) { "Only an introduced word can be paused, but was $this." }
    }

    val poolStatus: QuizPoolStatus
        get() = when {
            isPaused -> QuizPoolStatus.PAUSED
            introducedAt == null -> QuizPoolStatus.QUEUED
            else -> QuizPoolStatus.ACTIVE
        }
}

enum class QuizPoolStatus { QUEUED, ACTIVE, PAUSED }
