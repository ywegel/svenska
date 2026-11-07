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
    @MethodSource("provideDetermineWordGroupTestCases", "provideRivstartNounTestCases")
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
                // Unknown endings without a verb supine
                Arguments.of("example", listOf("-x", "-y", "-z"), WordGroup.Noun(WordGroup.NounSubgroup.UNDEFINED)),
                // Noun with verb ending
                Arguments.of("word", listOf("-n", "-ar", "-en"), WordGroup.Noun(WordGroup.NounSubgroup.AR)),
                // Noun with verb ending
                Arguments.of("word", listOf("-n", "-er", "-na"), WordGroup.Noun(WordGroup.NounSubgroup.ER)),
                // No endings
                Arguments.of("word", emptyList<String>(), WordGroup.Other),

                // Nouns without a plural only have the definite singular
                Arguments.of("mat", listOf("-en"), WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("socker", listOf("-n"), WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("trafik", listOf("-et"), WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("kaffe", listOf("-t"), WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("skam", listOf("-men"), WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
                // Adjectives ending on -t only list the plural
                Arguments.of("trött", listOf("-a"), WordGroup.Adjective),
                Arguments.of("bäst", listOf("-a"), WordGroup.Adjective),
                // Single endings that are neither a definite singular nor an adjective plural
                Arguments.of("snabb", listOf("a"), WordGroup.Other),
                Arguments.of("kläder", listOf("-na"), WordGroup.Other),
                Arguments.of("livstid", listOf("-s"), WordGroup.Other),
                Arguments.of("morgonfika", listOf("-t/-n"), WordGroup.Other),
                Arguments.of("stan", listOf("vard. för staden"), WordGroup.Other),
            )
        }

        @JvmStatic
        fun provideRivstartNounTestCases(): Stream<Arguments> {
            fun noun(word: String, endings: String, subgroup: WordGroup.NounSubgroup) =
                Arguments.of(word, endings.split(" "), WordGroup.Noun(subgroup))

            return Stream.of(
                // Group 1: -or
                noun("våg", "-en -or -orna", WordGroup.NounSubgroup.OR),
                noun("ros", "-en -or -orna", WordGroup.NounSubgroup.OR),
                noun("sida", "-n sidor sidorna", WordGroup.NounSubgroup.OR),
                noun("flicka", "-an flickor flickorna", WordGroup.NounSubgroup.OR),

                // Group 2: -ar
                noun("bil", "-en -ar -arna", WordGroup.NounSubgroup.AR),
                noun("vägg", "-en -ar -arna", WordGroup.NounSubgroup.AR),
                noun("topp", "-en -ar -arna", WordGroup.NounSubgroup.AR),
                noun("bro", "-n -ar -arna", WordGroup.NounSubgroup.AR),
                noun("by", "-n -ar -arna", WordGroup.NounSubgroup.AR),
                noun("ö", "-n -ar -arna", WordGroup.NounSubgroup.AR),
                noun("å", "-n -ar -arna", WordGroup.NounSubgroup.AR),
                noun("timme", "-n timmar timmarna", WordGroup.NounSubgroup.AR),
                noun("hare", "-n harar hararna", WordGroup.NounSubgroup.AR),
                noun("cykel", "-n cyklar cyklarna", WordGroup.NounSubgroup.AR),
                noun("minister", "-n ministrar ministrarna", WordGroup.NounSubgroup.AR),
                noun("parkering", "-en -ar -arna", WordGroup.NounSubgroup.AR),
                noun("kompis", "-en -ar -arna", WordGroup.NounSubgroup.AR),
                noun("sjukdom", "-en -ar -arna", WordGroup.NounSubgroup.AR),
                noun("storlek", "-en -ar -arna", WordGroup.NounSubgroup.AR),

                // Group 3: -er
                noun("maskerad", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("japan", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("demonstrant", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("student", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("tomat", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("konferens", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("protest", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("raritet", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("omelett", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("mobil", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("station", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("cyklist", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("biolog", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("kromosom", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("idiot", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("filosof", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("frisör", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("nyhet", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("motor", "-n -er -erna", WordGroup.NounSubgroup.ER),
                noun("kunskap", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("film", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("fest", "-en -er -erna", WordGroup.NounSubgroup.ER),
                noun("rad", "-en -er -arna", WordGroup.NounSubgroup.ER),
                noun("kö", "-n -er -erna", WordGroup.NounSubgroup.ER),
                noun("vy", "-n -er -erna", WordGroup.NounSubgroup.ER),
                noun("kafé", "-t -er -erna", WordGroup.NounSubgroup.ER),
                noun("pris", "-et -er -erna", WordGroup.NounSubgroup.ER),
                noun("bageri", "-t -er -erna", WordGroup.NounSubgroup.ER),
                noun("bonde", "-n bönder bönderna", WordGroup.NounSubgroup.ER),
                noun("te", "teet teer teerna", WordGroup.NounSubgroup.ER),

                // Group 4: -r
                noun("ko", "-n -r -rna", WordGroup.NounSubgroup.R),
                noun("sko", "-n skor skorna", WordGroup.NounSubgroup.R),
                noun("tå", "-n -r -rna", WordGroup.NounSubgroup.R),
                noun("same", "-n -er -erna", WordGroup.NounSubgroup.R),
                noun("serie", "-n -er -erna", WordGroup.NounSubgroup.R),
                noun("aktie", "-n -r -rna", WordGroup.NounSubgroup.R),
                noun("fiende", "-n -r -rna", WordGroup.NounSubgroup.R),
                noun("linje", "-n -r -rna", WordGroup.NounSubgroup.R),
                noun("bakelse", "-n -r -rna", WordGroup.NounSubgroup.R),
                noun("bibliotekarie", "-n -r -rna", WordGroup.NounSubgroup.R),

                // Group 5: -n
                noun("meddelande", "-t -n -na", WordGroup.NounSubgroup.N),
                noun("yrke", "-t -n -na", WordGroup.NounSubgroup.N),
                noun("bi", "-et -n -na", WordGroup.NounSubgroup.N),

                // Group 6a: unchanged ett-words
                noun("träd", "-et - -en", WordGroup.NounSubgroup.UNCHANGED_ETT),
                noun("program", "-met - -men", WordGroup.NounSubgroup.UNCHANGED_ETT),

                // Group 6b: unchanged en-words
                noun("målare", "-n - målarna", WordGroup.NounSubgroup.UNCHANGED_EN),
                noun("indier", "-n - -na", WordGroup.NounSubgroup.UNCHANGED_EN),
                noun("elektriker", "-n - -na", WordGroup.NounSubgroup.UNCHANGED_EN),
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
