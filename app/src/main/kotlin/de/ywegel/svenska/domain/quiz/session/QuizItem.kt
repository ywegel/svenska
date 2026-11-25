package de.ywegel.svenska.domain.quiz.session

import de.ywegel.svenska.data.model.Vocabulary

/**
 * One screen of the quiz for one word. Each case is one kind of screen, so the UI decides with one `when`.
 *
 * It only says what is asked. The FSRS state is loaded fresh when the answer is saved, so it can't go stale while the
 * item waits in the session.
 */
sealed interface QuizItem {
    val vocabulary: Vocabulary

    /** Type the Swedish word, then the forms if they are asked along. */
    data class DeSv(override val vocabulary: Vocabulary, val withForms: Boolean = false) : QuizItem

    /** Type the German translation. */
    data class SvDe(override val vocabulary: Vocabulary) : QuizItem

    /** The Swedish word is shown, only the forms are typed. */
    data class Forms(override val vocabulary: Vocabulary) : QuizItem
}
