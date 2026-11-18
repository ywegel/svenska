package de.ywegel.svenska.domain.words

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.testdata.ReferenceWords
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource

class WordGroupMatcherTest {
    @ParameterizedTest
    @MethodSource("referenceWordGroups", "wordGroupEdgeCases")
    fun testDetermineWordGroup(baseWord: String, endings: List<String>, expectedGroup: WordGroup) {
        val result = WordGroupMatcher.determineWordGroup(baseWord, endings)
        assertEquals(expectedGroup, result)
    }

    @ParameterizedTest
    @MethodSource("referenceGenders", "genderEdgeCases")
    fun testDetermineGender(endings: List<String>, expectedGender: Gender?) {
        val result = WordGroupMatcher.determineGender(
            WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED),
            endings,
        ) // Define as nouns, as non nouns always return null
        assertEquals(expectedGender, result)
    }

    @ParameterizedTest
    @MethodSource("suggestionCases")
    fun `suggest only suggests complete, known and different groups`(
        word: String,
        endings: List<String>,
        selected: WordGroup,
        expected: WordGroup?,
    ) {
        assertEquals(expected, WordGroupMatcher.suggest(word, endings, selected))
    }

    companion object {
        private val ar = WordGroup.Noun(WordGroup.NounSubgroup.AR)
        private val undefinedNoun = WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED)
        private val dreamEndings = listOf("-men", "-mar", "-marna")

        @JvmStatic
        fun suggestionCases(): List<Arguments> = listOf(
            argumentSet("other selected", "dröm", dreamEndings, WordGroup.Other, ar),
            argumentSet("noun without subgroup selected", "dröm", dreamEndings, undefinedNoun, ar),
            argumentSet("suggestion already selected", "dröm", dreamEndings, ar, null),
            argumentSet(
                "second of three noun endings is no complete adjective",
                "dröm",
                listOf("-men", "-mar"),
                undefinedNoun,
                null,
            ),
            argumentSet("incomplete endings", "dröm", listOf("-men"), WordGroup.Other, null),
            argumentSet("blank word", "", dreamEndings, WordGroup.Other, null),
        )

        // Abbreviated and written out endings have to lead to the same result
        @JvmStatic
        fun referenceWordGroups() = ReferenceWords.all.flatMap { word ->
            listOf(word.endings, word.forms).distinct().map { Arguments.of(word.word, it, word.wordGroup) }
        }

        @JvmStatic
        fun referenceGenders() = ReferenceWords.nouns.flatMap { word ->
            listOf(word.endings, word.forms).distinct().map { Arguments.of(it, word.gender) }
        }

        @JvmStatic
        fun wordGroupEdgeCases(): List<Arguments> = listOf(
            Arguments.of("hus", listOf("-et", "", "-en"), WordGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT)),
            // Unknown endings without a verb supine have no gender
            Arguments.of("example", listOf("-x", "-y", "-z"), WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
            // Noun with verb ending and a definite plural that does not match the plural
            Arguments.of("word", listOf("-n", "-ar", "-en"), WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
            Arguments.of("word", listOf("-n", "-er", "-na"), WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
            // No endings
            Arguments.of("word", emptyList<String>(), WordGroup.Other),
            // Single endings that are neither a definite singular nor an adjective plural
            Arguments.of("snabb", listOf("a"), WordGroup.Other),
            Arguments.of("kläder", listOf("-na"), WordGroup.Other),
            Arguments.of("stan", listOf("vard. för staden"), WordGroup.Other),
        )

        @JvmStatic
        fun genderEdgeCases(): List<Arguments> = listOf(
            // No endings
            Arguments.of(emptyList<String>(), null),
            // Unknown endings
            Arguments.of(listOf("-x", "-y", "-z"), null),
            // Mixed endings (Neutrum due to -et)
            Arguments.of(listOf("-et", "-er", "-en"), Gender.Neutra),
            // Mixed endings (Utrum due to -en)
            Arguments.of(listOf("-en", "-er", "-na"), Gender.Ultra),
        )
    }
}
