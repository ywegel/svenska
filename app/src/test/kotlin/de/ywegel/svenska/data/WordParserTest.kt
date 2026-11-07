package de.ywegel.svenska.data

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.wordImporter.WordParserImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class WordParserTest {

    @ParameterizedTest
    @MethodSource("abbreviatedFormsDataProvider")
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
    @MethodSource("writtenOutFormsDataProvider")
    fun `should detect word group of written out forms`(word: String, expectedGroup: WordGroup) {
        // Given
        val parser = WordParserImpl()

        // When
        val result = parser.parseWord(word, "translation", CONTAINER_ID)

        // Then
        assertEquals(expectedGroup, result.wordGroup)
    }

    @ParameterizedTest
    @MethodSource("genderDataProvider")
    fun `should detect gender correctly`(word: String, expectedGender: Gender?) {
        // Given
        val parser = WordParserImpl()

        // When
        val result = parser.parseWord(word, "translation", CONTAINER_ID)

        // Then
        assertEquals(expectedGender, result.gender)
    }

    companion object {
        private const val CONTAINER_ID = 99

        private fun noun(subgroup: WordGroup.NounSubgroup) = WordGroup.Noun(subgroup)
        private fun verb(subgroup: WordGroup.VerbSubgroup) = WordGroup.Verb(subgroup)

        @JvmStatic
        private fun writtenOutFormsDataProvider(): Stream<Arguments> {
            return Stream.of(
                // Regular nouns
                Arguments.of("sida (-n, sidor, sidorna)", noun(WordGroup.NounSubgroup.OR)),
                Arguments.of("timme (-n, timmar, timmarna)", noun(WordGroup.NounSubgroup.AR)),
                Arguments.of("hare (-n, harar, hararna)", noun(WordGroup.NounSubgroup.AR)),
                Arguments.of("hurtbulle (-n, bullar, bullarna)", noun(WordGroup.NounSubgroup.AR)),
                Arguments.of("semester (semestern, semestrar, semestrarna)", noun(WordGroup.NounSubgroup.AR)),
                Arguments.of("morgon (morgonen, morgnar, morgnarna)", noun(WordGroup.NounSubgroup.AR)),
                Arguments.of("museum (-et, museer, museerna)", noun(WordGroup.NounSubgroup.ER)),
                Arguments.of("målare (-n, -, målarna)", noun(WordGroup.NounSubgroup.UNCHANGED_EN)),
                Arguments.of("kapitel (kapitlet, -, kapitlen)", noun(WordGroup.NounSubgroup.UNCHANGED_ETT)),
                Arguments.of("titel (titeln, titlar, titlarna)", noun(WordGroup.NounSubgroup.AR)),
                Arguments.of("uttryck (-et, -, -en)", noun(WordGroup.NounSubgroup.UNCHANGED_ETT)),
                Arguments.of("konto (kontot, konton, kontona)", noun(WordGroup.NounSubgroup.N)),

                // Nouns with a vowel change in the plural
                Arguments.of("fot (-en, fötter, fötterna)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("dotter (-n, döttrar, döttrarna)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("land (-et, länder, länderna)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("man (mannen, män, männen)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("brandman (-nen, -män, -männen)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("mus (musen, möss, mössen)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("dagbok (-en, dagböcker, dagböckerna)", noun(WordGroup.NounSubgroup.SPECIAL)),

                // Regular nouns that look similar to typos
                Arguments.of("plåster (plåstret, plåster, plåstren)", noun(WordGroup.NounSubgroup.UNCHANGED_ETT)),
                Arguments.of("självkänsla (-n, självkänslor, -orna)", noun(WordGroup.NounSubgroup.OR)),

                // Nouns without a definite singular
                Arguments.of("examen (-, examina, examina)", noun(WordGroup.NounSubgroup.SPECIAL)),

                // Nouns with typos
                Arguments.of("block (et, -, -en)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("vaktmästare (n, -, vaktmästarna)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("skymning (-en, ar, -arna)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("smör (-et, -, en)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("diktatur (-en, -er, erna)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("tillfälle (-t, -n, - na)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("klass (-en, -er, -arna)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("tron (-en, -er, -ern)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("följdfråga (-n, följdfrågor, följfrågorna)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("spa (-t, -n, -a)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("fisk (-en, -, -ar)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("långlunch (-en, -, -er)", noun(WordGroup.NounSubgroup.SPECIAL)),

                // Regular verbs
                Arguments.of("svara (svarar, svarade, svarat)", verb(WordGroup.VerbSubgroup.GROUP_1)),
                Arguments.of("bygga (bygger, byggde, byggt)", verb(WordGroup.VerbSubgroup.GROUP_2A)),
                Arguments.of("köra (kör, körde, kört)", verb(WordGroup.VerbSubgroup.GROUP_2A)),
                Arguments.of("glömma (glömmer, glömde, glömt)", verb(WordGroup.VerbSubgroup.GROUP_2A)),
                Arguments.of("känna (-er, kände, känt)", verb(WordGroup.VerbSubgroup.GROUP_2A)),
                Arguments.of("köpa (köper, köpte, köpt)", verb(WordGroup.VerbSubgroup.GROUP_2B)),
                Arguments.of("byta (byter, bytte, bytt)", verb(WordGroup.VerbSubgroup.GROUP_2B)),
                Arguments.of("bo (bor, bodde, bott)", verb(WordGroup.VerbSubgroup.GROUP_3)),
                Arguments.of("må (-r, mådde, mått)", verb(WordGroup.VerbSubgroup.GROUP_3)),

                // Irregular verbs
                Arguments.of("ha (har, hade, haft)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)),
                Arguments.of("skriva (skriver, skrev, skrivit)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)),
                Arguments.of("välja (väljer, valde, valt)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)),
                Arguments.of("kunna (kan, kunde, kunnat)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)),
                Arguments.of("sätta (sätter, satte, satt)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)),
                Arguments.of("växa (växer, växte, vuxit)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)),
                Arguments.of("säga (säger, sade/sa, sagt)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)),
                Arguments.of("glädja (glädjer/gläder, gladde, glatt)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)),
            )
        }

        @JvmStatic
        private fun abbreviatedFormsDataProvider(): Stream<Arguments> = Stream.of(
            Arguments.of("penna (-n, -or, -orna)", noun(WordGroup.NounSubgroup.OR), "-n -or -orna"),
            Arguments.of("markera (-r, -de, -t)", verb(WordGroup.VerbSubgroup.GROUP_1), "-r -de -t"),
            Arguments.of("plugga (-r, -de, -t)", verb(WordGroup.VerbSubgroup.GROUP_1), "-r -de -t"),
            Arguments.of("läsa (-er, -te, -t)", verb(WordGroup.VerbSubgroup.GROUP_2B), "-er -te -t"),
            Arguments.of("mening (-en, -ar, -arna)", noun(WordGroup.NounSubgroup.AR), "-en -ar -arna"),
            Arguments.of("göra (gör, gjorde, gjort)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL), "gör gjorde gjort"),
            Arguments.of("vanlig (-t, -a)", WordGroup.Adjective, "-t -a"),
            Arguments.of("generell (-t, -a)", WordGroup.Adjective, "-t -a"),
            Arguments.of("konto (-t, -n, -na)", noun(WordGroup.NounSubgroup.N), "-t -n -na"),
            Arguments.of("spel (-et, -, -en)", noun(WordGroup.NounSubgroup.UNCHANGED_ETT), "-et - -en"),

            // Ported from "writtenOutFormsDataProvider". Words that don't have an abbreviated version are discarded.
            // Regular nouns
            Arguments.of("sida (-n, sidor, sidorna)", noun(WordGroup.NounSubgroup.OR), "-n sidor sidorna"),
            Arguments.of("timme (-n, timmar, timmarna)", noun(WordGroup.NounSubgroup.AR), "-n timmar timmarna"),
            Arguments.of("hare (-n, harar, hararna)", noun(WordGroup.NounSubgroup.AR), "-n harar hararna"),
            Arguments.of("hurtbulle (-n, bullar, bullarna)", noun(WordGroup.NounSubgroup.AR), "-n bullar bullarna"),
            Arguments.of("museum (-et, museer, museerna)", noun(WordGroup.NounSubgroup.ER), "-et museer museerna"),
            Arguments.of("målare (-n, -, målarna)", noun(WordGroup.NounSubgroup.UNCHANGED_EN), "-n - målarna"),
            Arguments.of("uttryck (-et, -, -en)", noun(WordGroup.NounSubgroup.UNCHANGED_ETT), "-et - -en"),
            // Nouns with a vowel change in the plural
            Arguments.of("fot (-en, fötter, fötterna)", noun(WordGroup.NounSubgroup.SPECIAL), "-en fötter fötterna"),
            Arguments.of(
                "dotter (-n, döttrar, döttrarna)",
                noun(WordGroup.NounSubgroup.SPECIAL),
                "-n döttrar döttrarna",
            ),
            Arguments.of("land (-et, länder, länderna)", noun(WordGroup.NounSubgroup.SPECIAL), "-et länder länderna"),
            Arguments.of("brandman (-nen, -män, -männen)", noun(WordGroup.NounSubgroup.SPECIAL), "-nen -män -männen"),
            Arguments.of(
                "dagbok (-en, dagböcker, dagböckerna)",
                noun(WordGroup.NounSubgroup.SPECIAL),
                "-en dagböcker dagböckerna",
            ),
            // Regular nouns that look similar to typos
            Arguments.of(
                "självkänsla (-n, självkänslor, -orna)",
                noun(WordGroup.NounSubgroup.OR),
                "-n självkänslor -orna",
            ),
            // Nouns with typos
            Arguments.of("block (-et, -, -en)", noun(WordGroup.NounSubgroup.UNCHANGED_ETT), "-et - -en"),
            Arguments.of("block (et, -, -en)", noun(WordGroup.NounSubgroup.SPECIAL), "et - -en"),
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
            // Regular verbs
            Arguments.of("svara (-r, -de, -t)", verb(WordGroup.VerbSubgroup.GROUP_1), "-r -de -t"),
            // Nouns without a plural
            Arguments.of("mat  (-en)", noun(WordGroup.NounSubgroup.SPECIAL), "-en"),
            Arguments.of("kaffe  (-t)", noun(WordGroup.NounSubgroup.SPECIAL), "-t"),
            Arguments.of("skam  (-men)", noun(WordGroup.NounSubgroup.SPECIAL), "-men"),
            // Adjectives without a neuter form
            Arguments.of("trött  (-a)", WordGroup.Adjective, "-a"),
            Arguments.of("smart  (-a)  mobil  (-er)", WordGroup.Adjective, "-a"),
        )

        @JvmStatic
        private fun genderDataProvider(): Stream<Arguments> = Stream.of(
            // En-words: definite singular ends with n
            Arguments.of("penna (-n, -or, -orna)", Gender.Ultra),
            Arguments.of("mening (-en, -ar, -arna)", Gender.Ultra),
            Arguments.of("semester (semestern, semestrar, semestrarna)", Gender.Ultra),
            Arguments.of("målare (-n, -, målarna)", Gender.Ultra),

            // Ett-words: definite singular ends with t
            Arguments.of("konto (-t, -n, -na)", Gender.Neutra),
            Arguments.of("spel (-et, -, -en)", Gender.Neutra),
            Arguments.of("kapitel (kapitlet, -, kapitlen)", Gender.Neutra),
            Arguments.of("museum (-et, museer, museerna)", Gender.Neutra),

            // Nouns without a plural
            Arguments.of("mat (-en)", Gender.Ultra),
            Arguments.of("skam (-men)", Gender.Ultra),
            Arguments.of("kaffe (-t)", Gender.Neutra),
            Arguments.of("trafik (-en)", Gender.Ultra),

            // No definite singular or unknown ending
            Arguments.of("examen (-, examina, examina)", null),

            // Only nouns have a gender
            Arguments.of("markera (-r, -de, -t)", null),
            Arguments.of("göra (gör, gjorde, gjort)", null),
            Arguments.of("vanlig (-t, -a)", null),
        )
    }
}
