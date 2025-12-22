package de.ywegel.svenska.data

import de.ywegel.svenska.domain.wordImporter.WordExtractor.extractWordAndEndings
import de.ywegel.svenska.domain.wordImporter.WordExtractor.normalizePdfDashes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.stream.Stream

class WordExtractorTest {
    @ParameterizedTest
    @MethodSource("provideExtractWordAndEndingsTestCases")
    fun testExtractWordAndEndings(input: String, expectedWord: String, expectedEndings: List<String>) {
        val result = extractWordAndEndings(input)
        assertEquals(expectedWord, result.first)
        assertEquals(expectedEndings, result.second)
    }

    @Nested
    inner class NormalizePdfDashes {

        @Test
        fun `should replace unicode minus sign with hyphen-minus`() {
            // Given
            val input = "a\u2212b" // a−b

            // When
            val result = input.normalizePdfDashes()

            // Then
            expectThat(result).isEqualTo("a-b")
        }

        @Test
        fun `should replace en dash with hyphen-minus`() {
            // Given
            val input = "a\u2013b" // a–b

            // When
            val result = input.normalizePdfDashes()

            // Then
            expectThat(result).isEqualTo("a-b")
        }

        @Test
        fun `should replace em dash with hyphen-minus`() {
            // Given
            val input = "a\u2014b" // a—b

            // When
            val result = input.normalizePdfDashes()

            // Then
            expectThat(result).isEqualTo("a-b")
        }

        @Test
        fun `should replace hyphen (U+2010) with hyphen-minus`() {
            // Given
            val input = "a\u2010b" // a‐b

            // When
            val result = input.normalizePdfDashes()

            // Then
            expectThat(result).isEqualTo("a-b")
        }

        @Test
        fun `should handle mixed dashes correctly`() {
            // Given
            val input = "test\u2212one\u2013two\u2014three"

            // When
            val result = input.normalizePdfDashes()

            // Then
            expectThat(result).isEqualTo("test-one-two-three")
        }

        @Test
        fun `should leave normal hyphen-minus untouched`() {
            // Given
            val input = "normal-hyphen-test"

            // When
            val result = input.normalizePdfDashes()

            // Then
            expectThat(result).isEqualTo("normal-hyphen-test")
        }

        @Test
        fun `should return unchanged string when no special dashes present`() {
            // Given
            val input = "clean string without dashes"

            // When
            val result = input.normalizePdfDashes()

            // Then
            expectThat(result).isEqualTo(input)
        }

        @Test
        fun `should handle empty string`() {
            // Given
            val input = ""

            // When
            val result = input.normalizePdfDashes()

            // Then
            expectThat(result).isEqualTo("")
        }
    }

    companion object {
        @JvmStatic
        fun provideExtractWordAndEndingsTestCases(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("penna (-n, -or, -orna)", "penna", listOf("-n", "-or", "-orna")),
                Arguments.of("mobil (-en, -er, -erna)", "mobil", listOf("-en", "-er", "-erna")),
                Arguments.of("söka (-er, -te, -t)", "söka", listOf("-er", "-te", "-t")),
                Arguments.of("land (-et, länder, länderna)", "land", listOf("-et", "länder", "länderna")),
                Arguments.of("svensk (-t, -a)", "svensk", listOf("-t", "-a")),

                // Edge cases
                // No endings
                Arguments.of("example", "example", emptyList<String>()),
                // Empty input
                Arguments.of("", "", emptyList<String>()),
                // Empty parentheses
                Arguments.of("word ()", "word ()", emptyList<String>()),
                // Empty endings in parentheses
                Arguments.of("word (,,)", "word", listOf("", "", "")),
                // Dash endings in parentheses
                Arguments.of("word (-, -, -)", "word", listOf("-", "-", "-")),
                // Only one ending
                Arguments.of("word (-n)", "word", listOf("-n")),
                // Whitespace noise
                Arguments.of("  penna   (  -n  ,  -or  ,   -orna   )   ", "penna", listOf("-n", "-or", "-orna")),
            )
        }
    }
}
