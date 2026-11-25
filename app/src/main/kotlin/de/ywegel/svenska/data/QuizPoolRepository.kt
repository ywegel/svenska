package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.quiz.PoolRemoval

interface QuizPoolRepository {
    /**
     * Removes the word from the quiz pool. A queued word is deleted, as it has no progress yet. An active word is
     * paused and keeps its progress. A paused word or a word that is not in the pool stays as it is.
     */
    suspend fun removeFromPool(vocabularyId: Int): PoolRemoval
}
