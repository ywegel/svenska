package de.ywegel.svenska.domain.quiz.compare

import kotlin.math.abs

sealed interface AnswerVerdict {
    data object Correct : AnswerVerdict

    data class Typo(val alignment: List<EditStep>) : AnswerVerdict

    data object Wrong : AnswerVerdict
}

/**
 * Decides whether a typed answer is correct, close enough to count as a typo, or wrong.
 * Typing a different known word is never a typo, even if it is only one letter away: andra is no typo of ändra.
 */
class AnswerChecker(knownWords: Collection<String>) {
    private val knownWords = knownWords.mapTo(HashSet()) { it.comparable() }

    fun check(typed: String, expected: String): AnswerVerdict {
        val typedWords = typed.words()
        val expectedWords = expected.words()
        if (typedWords.size != expectedWords.size) return AnswerVerdict.Wrong

        val alignment = typedWords
            .zip(expectedWords) { t, e ->
                when (val verdict = checkWord(t, e)) {
                    AnswerVerdict.Wrong -> return AnswerVerdict.Wrong
                    AnswerVerdict.Correct -> listOf(EditStep.Match(t, e))
                    is AnswerVerdict.Typo -> verdict.alignment
                }
            }
            .reduce { joined, word -> joined + EditStep.Match(" ", " ") + word }

        return if (alignment.all { it is EditStep.Match }) AnswerVerdict.Correct else AnswerVerdict.Typo(alignment)
    }

    // check -> checkWord
    private fun checkWord(typed: String, expected: String): AnswerVerdict {
        val comparableTyped = typed.comparable()
        val comparableExpected = expected.comparable()
        val allowedEdits = allowedEdits(comparableExpected.length)

        return when {
            comparableTyped == comparableExpected -> AnswerVerdict.Correct
            comparableTyped in knownWords -> AnswerVerdict.Wrong
            // Each letter of length difference costs at least one edit, so the table is not needed
            abs(comparableTyped.length - comparableExpected.length) > allowedEdits -> AnswerVerdict.Wrong
            else -> typoOrWrong(typed.trim(), expected.trim(), allowedEdits)
        }
    }

    private fun typoOrWrong(typed: String, expected: String, allowedEdits: Int): AnswerVerdict {
        val alignment = OptimalStringAlignment.alignment(typed, expected)
        val edits = alignment.count { it !is EditStep.Match }
        return if (edits <= allowedEdits) AnswerVerdict.Typo(alignment) else AnswerVerdict.Wrong
    }

    // One edit is a third of a three letter word, so short words like bil or hus get no tolerance
    private fun allowedEdits(expectedLength: Int): Int = when {
        expectedLength <= SHORT_WORD_MAX_LENGTH -> 0
        expectedLength <= MEDIUM_WORD_MAX_LENGTH -> 1
        else -> 2
    }

    // Hyphens and apostrophes stay part of the word
    private val wordRegex = Regex("""[^\s.,!?;:]+""")

    private fun String.words(): List<String> = wordRegex.findAll(this).map { it.value }.toList()

    private companion object {
        const val SHORT_WORD_MAX_LENGTH = 3
        const val MEDIUM_WORD_MAX_LENGTH = 7
    }
}
