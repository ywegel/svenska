package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.QuizPoolRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.VocabularyPoolState
import javax.inject.Inject
import kotlin.time.Instant

class ToggleVocabularyPoolUseCase @Inject constructor(
    private val queueToPool: QueueToPoolUseCase,
    private val quizPoolRepository: QuizPoolRepository,
) {
    suspend operator fun invoke(vocabulary: Vocabulary, poolState: VocabularyPoolState, now: Instant) {
        when (poolState) {
            VocabularyPoolState.NotInPool -> queueToPool(listOf(vocabulary), now)
            VocabularyPoolState.Paused -> quizPoolRepository.resumeInPool(vocabulary.id)
            VocabularyPoolState.Queued, is VocabularyPoolState.Active -> {
                quizPoolRepository.removeFromPool(vocabulary.id)
            }
        }
    }
}
