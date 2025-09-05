package de.ywegel.svenska.ui.common.vocabulary

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.first
import strikt.assertions.isA
import strikt.assertions.isEqualTo
import strikt.assertions.isFailure
import strikt.assertions.isNotNull
import strikt.assertions.isSuccess
import strikt.assertions.message
import strikt.assertions.second

class HighlightUtilsTest {

    @Nested
    @DisplayName("ParseHighlights")
    inner class ParseHighlightsTest {

        @ParameterizedTest(name = "parseHighlights succeeds for balanced highlights: {0}")
        @MethodSource("de.ywegel.svenska.ui.common.vocabulary.HighlightUtilsTest#balancedHighlightCases")
        fun `parseHighlights handles balanced highlights correctly`(
            input: String,
            expectedWord: String,
            expectedRanges: List<Pair<Int, Int>>,
        ) {
            // When
            val result = HighlightUtils.parseHighlights(input)

            // Then
            expectThat(result).isSuccess().and {
                first.isEqualTo(expectedWord)
                second.isEqualTo(expectedRanges)
            }
        }

        @ParameterizedTest(name = "parseHighlights fails for unbalanced highlights: {0}")
        @MethodSource("de.ywegel.svenska.ui.common.vocabulary.HighlightUtilsTest#unbalancedHighlightCases")
        fun `parseHighlights fails on unbalanced stars`(input: String, expectedStarCount: Int) {
            // When
            val result = HighlightUtils.parseHighlights(input)

            // Then
            expectThat(result).isFailure().and {
                this.isA<UnbalancedHighlightsError>().and {
                    this.message.isNotNull().contains(expectedStarCount.toString())
                }
            }
        }
    }

    @Nested
    @DisplayName("reconstructWithStars")
    inner class ReconstructWithStarsTest {
        @ParameterizedTest(name = "reconstructWithStars succeeds for valid ranges: {0} with {1}")
        @MethodSource("de.ywegel.svenska.ui.common.vocabulary.HighlightUtilsTest#validReconstructCases")
        fun `reconstructWithStars handles valid ranges correctly`(
            word: String,
            ranges: List<Pair<Int, Int>>,
            expected: String,
        ) {
            // When
            val result = HighlightUtils.reconstructWithStars(word, ranges)

            // Then
            expectThat(result).isEqualTo(expected)
        }

        @ParameterizedTest(name = "reconstructWithStars skips invalid ranges: {0} with {1}")
        @MethodSource("de.ywegel.svenska.ui.common.vocabulary.HighlightUtilsTest#invalidReconstructCases")
        fun `reconstructWithStars skips invalid ranges`(
            word: String,
            ranges: List<Pair<Int, Int>>,
            expected: String,
        ) {
            // When
            val result = HighlightUtils.reconstructWithStars(word, ranges)

            // Then
            expectThat(result).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("HighlightUtil round trips")
    inner class RoundTripTests {
        @ParameterizedTest(name = "round-trip: parseHighlights then reconstructWithStars recovers original input: {0}")
        @MethodSource("de.ywegel.svenska.ui.common.vocabulary.HighlightUtilsTest#balancedHighlightCases")
        fun `round-trip parseHighlights and reconstructWithStars recovers original input`(
            input: String,
            intermediate: String,
            intermediateHighlights: List<Pair<Int, Int>>,
        ) {
            // Given
            val parseResult = HighlightUtils.parseHighlights(input)

            // When
            val reconstructed = parseResult.getOrNull()?.let { (word, ranges) ->
                HighlightUtils.reconstructWithStars(word, ranges)
            }

            // Then
            expect {
                that(reconstructed).isEqualTo(input)
                that(parseResult).isSuccess().and {
                    first.isEqualTo(intermediate)
                    second.isEqualTo(intermediateHighlights)
                }
            }
        }

        @ParameterizedTest(name = "reverse round-trip: reconstructWithStars then parseHighlights recovers original word and ranges: {0} with {1}")
        @MethodSource("de.ywegel.svenska.ui.common.vocabulary.HighlightUtilsTest#balancedHighlightCases")
        fun `reverse round-trip reconstructWithStars and parseHighlights recovers original input`(
            intermediate: String,
            input: String,
            inputHighlights: List<Pair<Int, Int>>,
        ) {
            // Given
            val reconstructed = HighlightUtils.reconstructWithStars(input, inputHighlights)

            // When
            val parseResult = HighlightUtils.parseHighlights(reconstructed)

            // Then
            expect {
                that(reconstructed).isEqualTo(intermediate)
                that(parseResult).isSuccess().and {
                    first.isEqualTo(input)
                    second.isEqualTo(inputHighlights)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun balancedHighlightCases(): List<Arguments> = listOf(
            Arguments.of("hello", "hello", emptyList<Pair<Int, Int>>()),
            Arguments.of("fr*å*ga", "fråga", listOf(2 to 3)),
            Arguments.of("t*abc*est", "tabcest", listOf(1 to 4)),
            Arguments.of("a*b*c*d*e", "abcde", listOf(1 to 2, 3 to 4)),
            Arguments.of("*ab*cd", "abcd", listOf(0 to 2)),
            Arguments.of("ab*cd*", "abcd", listOf(2 to 4)),
            Arguments.of("a*bc*de*f*", "abcdef", listOf(1 to 3, 5 to 6)),
            Arguments.of("", "", emptyList<Pair<Int, Int>>()),
            Arguments.of("**", "", listOf(0 to 0)),
            Arguments.of("a**b", "ab", listOf(1 to 1)),
            Arguments.of("****", "", listOf(0 to 0, 0 to 0)),
            Arguments.of("a*b*", "ab", listOf(1 to 2)),
            Arguments.of("*b*", "b", listOf(0 to 1)),
            Arguments.of("x*y***a*b*", "xyab", listOf(1 to 2, 2 to 2, 3 to 4)),
            Arguments.of("****content", "content", listOf(0 to 0, 0 to 0)),
            Arguments.of("*a*b*cd*e*f*g*h*", "abcdefgh", listOf(0 to 1, 2 to 4, 5 to 6, 7 to 8)),
        )

        @JvmStatic
        fun unbalancedHighlightCases(): List<Arguments> = listOf(
            Arguments.of("*", 1),
            Arguments.of("fr*å*ga*", 3),
            Arguments.of("*fråga", 1),
            Arguments.of("***", 3),
            Arguments.of("a*bc", 1),
            Arguments.of("a*b*c**d*", 5),
        )

        @JvmStatic
        fun validReconstructCases(): List<Arguments> = listOf(
            Arguments.of("hello", emptyList<Pair<Int, Int>>(), "hello"),
            Arguments.of("fråga", listOf(2 to 3), "fr*å*ga"),
            Arguments.of("tabcest", listOf(1 to 4), "t*abc*est"),
            Arguments.of("abcde", listOf(0 to 1, 3 to 5), "*a*bc*de*"),
            Arguments.of("abcd", listOf(0 to 2), "*ab*cd"),
            Arguments.of("abcd", listOf(2 to 4), "ab*cd*"),
            Arguments.of("ab", listOf(1 to 1), "a**b"),
            Arguments.of("content", listOf(0 to 0, 0 to 0), "****content"),
            Arguments.of("xyab", listOf(1 to 2, 2 to 2, 3 to 4), "x*y***a*b*"),
        )

        @JvmStatic
        fun invalidReconstructCases(): List<Arguments> = listOf(
            // Negative start index
            Arguments.of("abc", listOf(-1 to 1), "abc"),
            // Negative end index
            Arguments.of("abc", listOf(0 to -1), "abc"),
            // End < start
            Arguments.of("abc", listOf(2 to 1), "abc"),
            // Start out of bounds
            Arguments.of("abc", listOf(4 to 4), "abc"),
            // End out of bounds
            Arguments.of("abc", listOf(0 to 4), "abc"),
            // Empty word with non-zero range
            Arguments.of("", listOf(0 to 1), ""),
            // Mixed valid and invalid ranges
            Arguments.of("abc", listOf(-1 to 1, 3 to 4, 1 to 2), "a*b*c"), // Only (1 to 2) is valid
        )
    }
}