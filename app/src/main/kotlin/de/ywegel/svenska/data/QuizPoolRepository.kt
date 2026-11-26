package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.PoolRemoval
import de.ywegel.svenska.data.model.quiz.QuizCard
import kotlin.time.Instant

interface QuizPoolRepository {
    /**
     * Adds [words] to the quiz pool as queued and saves their forms.
     * All words get [now] as their queue time, so words added together are introduced in the order of their id. A word
     * that is already in the pool, also a paused one, keeps its entry.
     */
    suspend fun queueToPool(words: List<Vocabulary>, now: Instant)

    /**
     * Removes the word from the quiz pool. A queued word is deleted, as it has no progress yet. An active word is
     * paused and keeps its progress. A paused word or a word that is not in the pool stays as it is.
     */
    suspend fun removeFromPool(vocabularyId: Int): PoolRemoval

    /**
     * The words and forms cards introduced since [since]. Both count towards the same daily limit of new cards.
     */
    suspend fun countIntroducedSince(since: Instant): Int

    /**
     * Active rows whose forms card can be introduced as far as the pool knows: a streak of at least [minSvDeStreak], no
     * `formsNeedReview`, forms, and no forms card yet, also not through another row with the same forms. Longest
     * introduced first.
     *
     * Rows that share their forms all come, so the caller can pick one per forms key after deciding which rows have
     * forms worth asking.
     */
    suspend fun formsCandidates(minSvDeStreak: Int): List<Vocabulary>

    /**
     * The next [limit] queued words, first in, first out.
     */
    suspend fun queuedWords(limit: Int): List<Vocabulary>

    /**
     * Introduces [words] with their SV_DE card and [forms] with their shared forms card in one transaction. The new
     * cards are due at [now].
     *
     * @return The new cards.
     */
    suspend fun introduce(words: List<Vocabulary>, forms: List<Vocabulary>, now: Instant): List<QuizCard>
}
