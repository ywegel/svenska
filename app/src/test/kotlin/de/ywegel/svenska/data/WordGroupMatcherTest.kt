package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.wordImporter.WordGroupMatcher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class WordGroupMatcherTest {
    @ParameterizedTest
    @MethodSource("provideDetermineWordGroupTestCases")
    fun testDetermineWordGroup(baseWord: String, endings: List<String>, expectedGroup: WordGroup) {
        val result = WordGroupMatcher.determineWordGroup(baseWord, endings)
        assertEquals(expectedGroup, result)
    }

    @Suppress("detekt:UnusedParameter")
    @ParameterizedTest
    @MethodSource("provideDetermineGenderTestCases")
    fun testDetermineGender(ignore: String, endings: List<String>, expectedGender: Gender?) {
        val result = WordGroupMatcher.determineGender(
            WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED),
            endings,
        ) // Define as nouns, as non nouns always return null
        assertEquals(expectedGender, result)
    }

    companion object {
        @Suppress("ktlint:standard:argument-list-wrapping")
        @JvmStatic
        fun provideDetermineWordGroupTestCases(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("penna", listOf("-n", "-or", "-orna"), WordGroup.Noun(WordGroup.NounSubgroup.OR)),
                Arguments.of("söka", listOf("-er", "-te", "-t"), WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2B)),
                Arguments.of("svensk", listOf("-t", "-a"), WordGroup.Adjective),
                Arguments.of("land", listOf("-et", "länder", "länderna"), WordGroup.Noun(WordGroup.NounSubgroup.ER)),
                Arguments.of("bo", listOf("-r", "-dde", "-tt"), WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_3)),
                Arguments.of("sko", listOf("-n", "-r", "-rna"), WordGroup.Noun(WordGroup.NounSubgroup.R)),
                Arguments.of("hus", listOf("-et", "-", "-en"), WordGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT)),
                Arguments.of("hus", listOf("-et", "", "-en"), WordGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT)),

                // Edge cases
                // Unknown verb
                Arguments.of(
                    "example",
                    listOf("-x", "-y", "-z"),
                    WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL),
                ),
                // Noun with verb ending
                Arguments.of("word", listOf("-n", "-ar", "-en"), WordGroup.Noun(WordGroup.NounSubgroup.AR)),
                // Noun with verb ending
                Arguments.of("word", listOf("-n", "-er", "-na"), WordGroup.Noun(WordGroup.NounSubgroup.ER)),
                // No endings
                Arguments.of("word", emptyList<String>(), WordGroup.Other),
            )
        }

        @JvmStatic
        fun provideDetermineGenderTestCases(): Stream<Arguments> = @Suppress("ktlint:standard:max-line-length")
        Stream.of(
            Arguments.of("penna", listOf("-n", "-or", "-orna"), Gender.Ultra),
            Arguments.of("land", listOf("-et", "länder", "länderna"), Gender.Neutra),
            Arguments.of("hus", listOf("-et", "hus", "husen"), Gender.Neutra),
            Arguments.of("cykel", listOf("-n", "-ar", "-arna"), Gender.Ultra),
            Arguments.of("flicka", listOf("-n", "-or", "-orna"), Gender.Ultra),
            Arguments.of("konto", listOf("-t", "-n", "-na"), Gender.Neutra),
            // Edge cases
            // No endings
            Arguments.of("example", emptyList<String>(), null),
            // Unknown endings (assume Utrum)
            Arguments.of("word", listOf("-x", "-y", "-z"), null),
            // Mixed endings (Neutrum due to -et)
            Arguments.of("word", listOf("-et", "-er", "-en"), Gender.Neutra),
            // Mixed endings (Utrum due to -en)
            Arguments.of("word", listOf("-en", "-er", "-na"), Gender.Ultra),
        )
    }
}
