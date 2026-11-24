package de.ywegel.svenska.data

import de.ywegel.svenska.data.db.quiz.QuizPoolEntry

interface QuizPoolRepository {
    /** Deletes a queued word, as it has no progress to keep, and pauses an active one. */
    suspend fun removeFromPool(entry: QuizPoolEntry)
}
