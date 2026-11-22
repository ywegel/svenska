package de.ywegel.svenska.domain.quiz.compare

import de.ywegel.svenska.domain.quiz.compare.EditStep.ExtraLetter
import de.ywegel.svenska.domain.quiz.compare.EditStep.Match
import de.ywegel.svenska.domain.quiz.compare.EditStep.MissingLetter
import de.ywegel.svenska.domain.quiz.compare.EditStep.SwappedLetters
import de.ywegel.svenska.domain.quiz.compare.EditStep.WrongLetter
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class OptimalStringAlignmentTest {
    @ParameterizedTest
    @MethodSource("distanceCases")
    fun `distance counts the edits between typed and expected`(typed: String, expected: String, distance: Int) {
        // When
        val result = OptimalStringAlignment.distance(typed, expected)

        // Then
        expectThat(result).isEqualTo(distance)
    }

    @ParameterizedTest
    @MethodSource("alignmentCases")
    fun `alignment lists the edit steps from typed to expected`(
        typed: String,
        expected: String,
        steps: List<EditStep>,
    ) {
        // When
        val result = OptimalStringAlignment.alignment(typed, expected)

        // Then
        expectThat(result).isEqualTo(steps)
    }

    @ParameterizedTest
    @MethodSource("wordPairs")
    fun `alignment has as many edits as the distance`(typed: String, expected: String) {
        // When
        val alignment = OptimalStringAlignment.alignment(typed, expected)

        // Then
        expectThat(alignment.count { it !is Match }).isEqualTo(OptimalStringAlignment.distance(typed, expected))
    }

    @ParameterizedTest
    @MethodSource("wordPairs")
    fun `alignment covers every letter of both words in order`(typed: String, expected: String) {
        // When
        val alignment = OptimalStringAlignment.alignment(typed, expected)

        // Then
        expectThat(alignment.joinToString("") { it.typed }).isEqualTo(typed)
        expectThat(alignment.joinToString("") { it.expected }).isEqualTo(expected)
    }

    companion object {
        private const val NFD_A_RING = "a\u030A"
        private const val NFD_A_DIAERESIS = "a\u0308"
        private const val NFD_O_DIAERESIS = "o\u0308"

        @JvmStatic
        fun distanceCases(): List<Arguments> = listOf(
            argumentSet("equal", "bilar", "bilar", 0),
            argumentSet("wrong letter", "bilor", "bilar", 1),
            argumentSet("extra letter", "billar", "bilar", 1),
            argumentSet("missing letter", "bilr", "bilar", 1),
            argumentSet("swapped letters", "bialr", "bilar", 1),
            argumentSet("both empty", "", "", 0),
            argumentSet("typed empty", "", "bil", 3),
            argumentSet("expected empty", "bil", "", 3),
            argumentSet("case is ignored", "Bilar", "bilar", 0),
            argumentSet("å ä ö are letters of their own", "sar", "sår", 1),
            argumentSet(
                "decomposed å ä ö equal composed ones",
                "s${NFD_A_RING}g ${NFD_A_DIAERESIS}r h${NFD_O_DIAERESIS}g",
                "såg är hög",
                0,
            ),
            argumentSet("a letter is never edited twice", "CA", "ABC", 3),
        )

        @JvmStatic
        fun alignmentCases(): List<Arguments> = listOf(
            argumentSet("equal", "bil", "bil", listOf(Match("b", "b"), Match("i", "i"), Match("l", "l"))),
            argumentSet("wrong letter", "bol", "bil", listOf(Match("b", "b"), WrongLetter("o", "i"), Match("l", "l"))),
            argumentSet(
                "extra letter",
                "bill",
                "bil",
                listOf(Match("b", "b"), Match("i", "i"), ExtraLetter("l"), Match("l", "l")),
            ),
            argumentSet("missing letter", "bl", "bil", listOf(Match("b", "b"), MissingLetter("i"), Match("l", "l"))),
            argumentSet(
                "swapped letters",
                "bialr",
                "bilar",
                listOf(Match("b", "b"), Match("i", "i"), SwappedLetters("al", "la"), Match("r", "r")),
            ),
            argumentSet("both empty", "", "", emptyList<EditStep>()),
            argumentSet("case is kept per side", "B", "b", listOf(Match("B", "b"))),
            argumentSet("decomposed letters become one step", NFD_A_RING, "å", listOf(Match("å", "å"))),
        )

        private val words = listOf("", "bil", "bilar", "bialr", "lbiar", "ändra", "andra", "CA", "ABC", "sjukhuset")

        @JvmStatic
        fun wordPairs(): List<Arguments> = words.flatMap { typed ->
            words.map { expected -> argumentSet("'$typed' for '$expected'", typed, expected) }
        }
    }
}
