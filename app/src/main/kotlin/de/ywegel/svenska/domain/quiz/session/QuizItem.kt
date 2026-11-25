package de.ywegel.svenska.domain.quiz.session

import de.ywegel.svenska.data.db.quiz.QuizTranslationDirection
import de.ywegel.svenska.data.model.Vocabulary
import one.felsen.fsrskt.fsrs6.FsrsCard

/**
 * One screen of the quiz: the translation, the forms, or both for the same word.
 */
data class QuizItem(
    val vocabulary: Vocabulary,
    val translation: Translation? = null,
    val forms: FsrsCard? = null,
) {
    data class Translation(val direction: QuizTranslationDirection, val card: FsrsCard)

    init {
        require(translation != null || forms != null) { "A quiz item asks at least one card, but was $this." }
        require(forms == null || translation == null || translation.direction == QuizTranslationDirection.DE_SV) {
            "Forms are only asked together with DE_SV, but was $this."
        }
    }
}
