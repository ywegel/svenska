package de.ywegel.svenska.fakes

import de.ywegel.svenska.data.QuizPoolRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.PoolRemoval
import de.ywegel.svenska.data.model.quiz.QuizCard
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.VocabularyPoolState
import de.ywegel.svenska.data.model.quiz.VocabularyWithPoolState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import one.felsen.fsrskt.fsrs6.FsrsCard
import kotlin.time.Instant

/**
 * Returns the given [formsCandidates] and [queuedWords] and records the calls in [queuedToPool], [removedFromPool],
 * [resumedInPool], [introducedWords], [introducedForms] and [countedSince]. Introducing returns new cards due at the given time.
 */
class QuizPoolRepositoryFake(
    private val introducedToday: Int = 0,
    private val formsCandidates: List<Vocabulary> = emptyList(),
    private val queuedWords: List<Vocabulary> = emptyList(),
    private val poolStates: Map<Int, VocabularyPoolState> = emptyMap(),
) : QuizPoolRepository {
    val queuedToPool = mutableListOf<Vocabulary>()
    val removedFromPool = mutableListOf<Int>()
    val resumedInPool = mutableListOf<Int>()
    val introducedWords = mutableListOf<Vocabulary>()
    val introducedForms = mutableListOf<Vocabulary>()
    var countedSince: Instant? = null
        private set

    override suspend fun queueToPool(words: List<Vocabulary>, now: Instant) {
        queuedToPool += words
    }

    override fun withPoolStates(vocabularies: Flow<List<Vocabulary>>): Flow<List<VocabularyWithPoolState>> =
        vocabularies.map { list ->
            list.map { VocabularyWithPoolState(it, poolStates[it.id] ?: VocabularyPoolState.NotInPool) }
        }

    override suspend fun removeFromPool(vocabularyId: Int): PoolRemoval {
        removedFromPool += vocabularyId
        return PoolRemoval.UNCHANGED
    }

    override suspend fun resumeInPool(vocabularyId: Int) {
        resumedInPool += vocabularyId
    }

    override suspend fun countIntroducedSince(since: Instant): Int {
        countedSince = since
        return introducedToday
    }

    override suspend fun formsCandidates(minSvDeStreak: Int): List<Vocabulary> = formsCandidates

    override suspend fun queuedWords(limit: Int): List<Vocabulary> = queuedWords.take(limit)

    override suspend fun introduce(words: List<Vocabulary>, forms: List<Vocabulary>, now: Instant): List<QuizCard> {
        introducedWords += words
        introducedForms += forms
        val newCard = FsrsCard(due = now)
        return words.map { QuizCard(it, QuizFacet.SV_DE, newCard) } + forms.map {
            QuizCard(
                vocabulary = it,
                facet = QuizFacet.FORMS,
                fsrs = newCard,
            )
        }
    }
}
