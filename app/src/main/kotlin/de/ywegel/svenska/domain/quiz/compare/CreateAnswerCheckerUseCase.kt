package de.ywegel.svenska.domain.quiz.compare

import de.ywegel.svenska.data.VocabularyRepository
import javax.inject.Inject

/**
 * Builds the [AnswerChecker] of a quiz session with the words of all vocabulary rows as known words. Built once per
 * session, as the vocabulary grows over time.
 */
class CreateAnswerCheckerUseCase @Inject constructor(
    private val vocabularyRepository: VocabularyRepository,
) {
    suspend operator fun invoke(): AnswerChecker =
        AnswerChecker(knownWords = vocabularyRepository.getAllVocabulariesSnapshot(containerId = null).map { it.word })
}
