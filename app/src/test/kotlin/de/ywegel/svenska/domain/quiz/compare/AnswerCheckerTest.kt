package de.ywegel.svenska.domain.quiz.compare

import de.ywegel.svenska.domain.quiz.compare.EditStep.Match
import de.ywegel.svenska.domain.quiz.compare.EditStep.SwappedLetters
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

class AnswerCheckerTest {
    private val checker = AnswerChecker(knownWords = listOf("andra", "ändra", "bil", "bilar", "s${NFD_A_RING}g"))

    @ParameterizedTest
    @MethodSource("correctCases")
    fun `same answer is correct`(typed: String, expected: String) {
        // When
        val result = checker.check(typed, expected)

        // Then
        expectThat(result).isEqualTo(AnswerVerdict.Correct)
    }

    @ParameterizedTest
    @MethodSource("typoCases")
    fun `few edits for the word length are a typo`(typed: String, expected: String) {
        // When
        val result = checker.check(typed, expected)

        // Then
        expectThat(result).isA<AnswerVerdict.Typo>()
    }

    @ParameterizedTest
    @MethodSource("wrongCases")
    fun `too many edits or another known word are wrong`(typed: String, expected: String) {
        // When
        val result = checker.check(typed, expected)

        // Then
        expectThat(result).isEqualTo(AnswerVerdict.Wrong)
    }

    @Test
    fun `typo carries the alignment to mark the wrong letters`() {
        // When
        val result = checker.check("bialr", "bilar")

        // Then
        expectThat(result).isEqualTo(
            AnswerVerdict.Typo(listOf(Match("b", "b"), Match("i", "i"), SwappedLetters("al", "la"), Match("r", "r"))),
        )
    }

    companion object {
        private const val NFD_A_RING = "a\u030A"

        @JvmStatic
        fun correctCases(): List<Arguments> = listOf(
            argumentSet("equal", "bilar", "bilar"),
            argumentSet("different case", "Bilar", "bilar"),
            argumentSet("surrounding whitespace", " bilar ", "bilar"),
            argumentSet("decomposed å", "s${NFD_A_RING}g", "såg"),
            argumentSet("empty", "", ""),
        )

        @JvmStatic
        fun typoCases(): List<Arguments> = listOf(
            argumentSet("swapped letters", "bialr", "bilar"),
            argumentSet("one edit in a four letter word", "kat", "katt"),
            argumentSet("one edit in a seven letter word", "flicor", "flickor"),
            argumentSet("two edits in an eight letter word", "arbtaer", "arbetare"),
            argumentSet("a for ä when the typed word is unknown", "hamta", "hämta"),
        )

        @JvmStatic
        fun wrongCases(): List<Arguments> = listOf(
            argumentSet("one edit in a three letter word", "bul", "bil"),
            argumentSet("two edits in a seven letter word", "flockar", "flickor"),
            argumentSet("three edits in an eight letter word", "arbetxyz", "arbetare"),
            argumentSet("length differs more than the tolerance", "bilarnas", "bilar"),
            argumentSet("another known word", "andra", "ändra"),
            argumentSet("another known word in different case", "Andra", "ändra"),
            argumentSet("another known word stored decomposed", "såg", "sågs"),
        )
    }
}
