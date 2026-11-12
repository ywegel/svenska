package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.wordImporter.WordParserImpl
import de.ywegel.svenska.testdata.ReferenceWords
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class WordParserTest {

    @ParameterizedTest
    @MethodSource("referenceWordGroups", "writtenOutReferenceWordGroups", "wordGroupEdgeCases")
    fun `should detect word group and endings`(word: String, expectedGroup: WordGroup, expectedEnding: String) {
        // Given
        val parser = WordParserImpl()

        // When
        val result = parser.parseWord(word, "translation", CONTAINER_ID)

        // Then
        assertEquals(expectedGroup, result.wordGroup)
        assertEquals(expectedEnding, result.ending)
    }

    @ParameterizedTest
    @MethodSource("referenceGenders", "genderEdgeCases")
    fun `should detect gender correctly`(word: String, expectedGender: Gender?) {
        // Given
        val parser = WordParserImpl()

        // When
        val result = parser.parseWord(word, "translation", CONTAINER_ID)

        // Then
        assertEquals(expectedGender, result.gender)
    }

    @ParameterizedTest
    @MethodSource("referenceWordParts", "wordPartsEdgeCases")
    fun `should store prefix, word and suffix separately`(
        word: String,
        expectedPrefix: String,
        expectedWord: String,
        expectedSuffix: String,
    ) {
        // Given
        val parser = WordParserImpl()

        // When
        val result = parser.parseWord(word, "translation", CONTAINER_ID)

        // Then
        assertEquals(expectedPrefix, result.wordPrefix)
        assertEquals(expectedWord, result.word)
        assertEquals(expectedSuffix, result.wordSuffix)
    }

    @Test
    fun `null word returns empty word`() {
        // Given
        val parser = WordParserImpl()

        // When
        val result = parser.parseWord(null, "translation", CONTAINER_ID)

        // Then
        assertEquals(WordGroup.Other, result.wordGroup)
        assertEquals("", result.ending)
    }

    @Test
    fun `null translation returns empty word`() {
        // Given
        val parser = WordParserImpl()

        // When
        val result = parser.parseWord("sida (-n, sidor, sidorna)", null, CONTAINER_ID)

        // Then
        assertEquals(WordGroup.Noun(WordGroup.NounSubgroup.OR), result.wordGroup)
        assertEquals("-n sidor sidorna", result.ending)
        assertEquals("", result.translation)
    }

    companion object {
        private const val CONTAINER_ID = 99

        private fun noun(subgroup: WordGroup.NounSubgroup) = WordGroup.Noun(subgroup)

        @JvmStatic
        fun referenceWordGroups() = ReferenceWords.all.map {
            Arguments.of(it.text, it.wordGroup, it.endings.joinToString(" "))
        }

        // Words with a prefix or suffix only have their source text
        @JvmStatic
        fun writtenOutReferenceWordGroups() = ReferenceWords.all
            .filter { it.prefix.isEmpty() && it.suffix.isEmpty() && it.forms != it.endings }
            .map {
                Arguments.of("${it.word} (${it.forms.joinToString(", ")})", it.wordGroup, it.forms.joinToString(" "))
            }

        @JvmStatic
        fun referenceGenders() = ReferenceWords.all.map { Arguments.of(it.text, it.gender) }

        @JvmStatic
        fun referenceWordParts() = ReferenceWords.all.map { Arguments.of(it.text, it.prefix, it.word, it.suffix) }

        @JvmStatic
        fun wordGroupEdgeCases(): List<Arguments> = listOf(
            // Nouns without a definite singular
            Arguments.of("examen (-, examina, examina)", noun(WordGroup.NounSubgroup.SPECIAL), "- examina examina"),
            // Nouns with typos
            Arguments.of("block (et, -, -en)", noun(WordGroup.NounSubgroup.SPECIAL), "et - -en"),
            Arguments.of(
                "vaktmästare (n, -, vaktmästarna)",
                noun(WordGroup.NounSubgroup.SPECIAL),
                "n - vaktmästarna",
            ),
            Arguments.of("skymning (-en, ar, -arna)", noun(WordGroup.NounSubgroup.SPECIAL), "-en ar -arna"),
            Arguments.of("smör (-et, -, en)", noun(WordGroup.NounSubgroup.SPECIAL), "-et - en"),
            Arguments.of("diktatur (-en, -er, erna)", noun(WordGroup.NounSubgroup.SPECIAL), "-en -er erna"),
            Arguments.of("tillfälle (-t, -n, - na)", noun(WordGroup.NounSubgroup.SPECIAL), "-t -n - na"),
            Arguments.of("klass (-en, -er, -arna)", noun(WordGroup.NounSubgroup.SPECIAL), "-en -er -arna"),
            Arguments.of("tron (-en, -er, -ern)", noun(WordGroup.NounSubgroup.SPECIAL), "-en -er -ern"),
            Arguments.of(
                "följdfråga (-n, följdfrågor, följfrågorna)",
                noun(WordGroup.NounSubgroup.SPECIAL),
                "-n följdfrågor följfrågorna",
            ),
            Arguments.of("spa (-t, -n, -a)", noun(WordGroup.NounSubgroup.SPECIAL), "-t -n -a"),
            Arguments.of("fisk (-en, -, -ar)", noun(WordGroup.NounSubgroup.SPECIAL), "-en - -ar"),
            Arguments.of("långlunch (-en, -, -er)", noun(WordGroup.NounSubgroup.SPECIAL), "-en - -er"),
            // Endings in the suffix are ignored
            Arguments.of("smart  (-a)  mobil  (-er)", WordGroup.Adjective, "-a"),
        )

        @JvmStatic
        fun genderEdgeCases(): List<Arguments> = listOf(
            // No definite singular
            Arguments.of("examen (-, examina, examina)", null),
        )

        @JvmStatic
        fun wordPartsEdgeCases(): List<Arguments> = listOf(
            // No endings
            Arguments.of("hej då", "", "hej då", ""),
        )
    }
}
