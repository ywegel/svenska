package de.ywegel.svenska.domain.quiz.compare

/**
 * One step of turning the typed answer into the expected one. Joining [typed] of all steps gives the typed answer,
 * joining [expected] gives the expected answer, so the UI can render either word with the wrong letters marked.
 */
sealed interface EditStep {
    val typed: String
    val expected: String

    /** Same letter, possibly in a different case. */
    data class Match(override val typed: String, override val expected: String) : EditStep

    data class WrongLetter(override val typed: String, override val expected: String) : EditStep

    data class ExtraLetter(override val typed: String) : EditStep {
        override val expected = ""
    }

    data class MissingLetter(override val expected: String) : EditStep {
        override val typed = ""
    }

    /** Two neighbouring letters in swapped order: [typed] "al" for [expected] "la". */
    data class SwappedLetters(override val typed: String, override val expected: String) : EditStep
}
