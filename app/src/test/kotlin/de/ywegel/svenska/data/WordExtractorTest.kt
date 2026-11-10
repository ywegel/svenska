package de.ywegel.svenska.data

import de.ywegel.svenska.domain.wordImporter.ExtractedWord
import de.ywegel.svenska.domain.wordImporter.WordExtractor.extractWord
import de.ywegel.svenska.domain.wordImporter.WordExtractor.normalizePdfDashes
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
    @MethodSource("provideExtractWordTestCases")
    fun testExtractWord(input: String, expected: ExtractedWord) {
        expectThat(extractWord(input)).isEqualTo(expected)
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
        fun provideExtractWordTestCases(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("penna (-n, -or, -orna)", word("penna", "-n", "-or", "-orna")),
                Arguments.of("mobil (-en, -er, -erna)", word("mobil", "-en", "-er", "-erna")),
                Arguments.of("söka (-er, -te, -t)", word("söka", "-er", "-te", "-t")),
                Arguments.of("land (-et, länder, länderna)", word("land", "-et", "länder", "länderna")),
                Arguments.of("svensk (-t, -a)", word("svensk", "-t", "-a")),

                // Prefix and suffix
                Arguments.of("ett fenomen (-et, -, -en)", word("fenomen", "-et", "-", "-en", prefix = "ett")),
                Arguments.of("ha (-r, hade, haft) ont i", word("ha", "-r", "hade", "haft", suffix = "ont i")),
                Arguments.of(
                    "en  hel  del bil (-en, -ar, -arna) i  stan",
                    word("bil", "-en", "-ar", "-arna", prefix = "en  hel  del", suffix = "i  stan"),
                ),
                // Bracket without a word directly before it is part of the prefix
                Arguments.of("(en) bil (-en, -ar, -arna)", word("bil", "-en", "-ar", "-arna", prefix = "(en)")),

                // Verb phrases inflect the first word
                Arguments.of(
                    "flyga drake (-er, flög, flugit)",
                    word("flyga", "-er", "flög", "flugit", suffix = "drake"),
                ),
                Arguments.of(
                    "gå ut med hunden (-r, gick, gått)",
                    word("gå", "-r", "gick", "gått", suffix = "ut med hunden"),
                ),
                Arguments.of("dö ut (dör, dog, dött)", word("dö", "dör", "dog", "dött", suffix = "ut")),
                Arguments.of(
                    "gifta sig (-er, -te, -t) med någon",
                    word("gifta", "-er", "-te", "-t", suffix = "sig med någon"),
                ),
                Arguments.of(
                    "säga till (någon) (-er, sa/sade, sagt)",
                    word("säga", "-er", "sa/sade", "sagt", suffix = "till (någon)"),
                ),
                // First valid bracket is used, the rest is suffix/prefix
                Arguments.of("tro (-r, -dde, -tt) (på)", word("tro", "-r", "-dde", "-tt", suffix = "(på)")),
                Arguments.of("x (a, b, c, d) tro (-r)", word("tro", "-r", prefix = "x (a, b, c, d)")),
                // Brackets with a single ending are explanations, if a bracket with several endings exists
                Arguments.of(
                    "tilläggspension (ATP) (-en, -er, -erna)",
                    word("tilläggspension", "-en", "-er", "-erna", suffix = "(ATP)"),
                ),
                Arguments.of("chips (plural)", word("chips", "plural")),
                // Attached brackets belong to the word, unless they start with a dash
                Arguments.of("fing(er) (-ret, fingrar, fingrarna)", word("fing(er)", "-ret", "fingrar", "fingrarna")),
                Arguments.of("tyska(-n)", word("tyska", "-n")),
                Arguments.of("frivillig(-t, -a)", word("frivillig", "-t", "-a")),
                Arguments.of("lägga (-er, la(de), lagt)", word("lägga", "-er", "la(de)", "lagt")),

                // Edge cases
                // No endings
                Arguments.of("example", word("example")),
                // Empty input
                Arguments.of("", word("")),
                // Empty parentheses
                Arguments.of("word ()", word("word ()")),
                // Too many endings
                Arguments.of("word (a, b, c, d)", word("word (a, b, c, d)")),
                // Empty endings in parentheses
                Arguments.of("word (,,)", word("word", "", "", "")),
                // Dash endings in parentheses
                Arguments.of("word (-, -, -)", word("word", "-", "-", "-")),
                // Only one ending
                Arguments.of("word (-n)", word("word", "-n")),
                // Whitespace noise
                Arguments.of("  penna   (  -n  ,  -or  ,   -orna   )   ", word("penna", "-n", "-or", "-orna")),
            )
        }

        private fun word(baseWord: String, vararg endings: String, prefix: String = "", suffix: String = "") =
            ExtractedWord(prefix = prefix, baseWord = baseWord, endings = endings.toList(), suffix = suffix)
    }
}
