package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.QuizPoolRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.words.FormBuilder
import de.ywegel.svenska.domain.words.splitEndings
import de.ywegel.svenska.domain.words.withForms
import javax.inject.Inject
import kotlin.time.Instant

class QueueToPoolUseCase @Inject constructor(
    private val quizPoolRepository: QuizPoolRepository,
) {
    suspend operator fun invoke(words: List<Vocabulary>, now: Instant) {
        quizPoolRepository.queueToPool(words.map { it.withBuiltFormsIfMissing() }, now)
    }
}

private fun Vocabulary.withBuiltFormsIfMissing(): Vocabulary =
    if (hasForms || wordGroup == WordGroup.Other) this
    else withForms(FormBuilder.build(word, splitEndings(ending), wordGroup))
