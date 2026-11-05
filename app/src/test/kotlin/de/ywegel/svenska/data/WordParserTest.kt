package de.ywegel.svenska.data

import de.ywegel.svenska.assertVocabularyEqualsIgnoringTimestamps
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.wordImporter.WordParserImpl
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class WordParserTest {

    @ParameterizedTest
    @MethodSource("detectWordTypeDataProvider")
    fun `should detect word type correctly`(testData: ParserTestData) {
        // Given
        val parser = WordParserImpl()

        // When
        val result = parser.parseWord(testData.word, testData.translation, CONTAINER_ID)

        println(result)

        // Then
        assertVocabularyEqualsIgnoringTimestamps(testData.expected, result)
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

    data class ParserTestData(
        val word: String,
        val translation: String,
        val expected: Vocabulary,
    )

    companion object {
        private const val CONTAINER_ID = 99

        @JvmStatic
        private fun writtenOutFormsDataProvider(): Stream<Arguments> {
            fun noun(subgroup: WordGroup.NounSubgroup) = WordGroup.Noun(subgroup)
            fun verb(subgroup: WordGroup.VerbSubgroup) = WordGroup.Verb(subgroup)

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
                Arguments.of("träningsresa (-n, -resor, resorna)", noun(WordGroup.NounSubgroup.OR)),
                Arguments.of("uttryck (-et, -en, -ena)", noun(WordGroup.NounSubgroup.N)),

                // Nouns without a definite singular
                Arguments.of("titel (titel, titlar, titlarna)", noun(WordGroup.NounSubgroup.SPECIAL)),
                Arguments.of("farmor (farmoder, farmödrar, farmödrarna)", noun(WordGroup.NounSubgroup.SPECIAL)),
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
                Arguments.of("glädja (glädjer, gladde, glatt)", verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL)),
            )
        }

        @JvmStatic
        private fun detectWordTypeDataProvider(): Stream<Arguments> = Stream.of(
            Arguments.of(
                ParserTestData(
                    word = "penna (-n, -or, -orna)",
                    translation = "Stift",
                    expected = Vocabulary(
                        word = "penna",
                        translation = "Stift",
                        gender = Gender.Ultra,
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.OR),
                        ending = "-n -or -orna",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
            Arguments.of(
                ParserTestData(
                    word = "markera (-r, -de, -t)",
                    translation = "markieren",
                    expected = Vocabulary(
                        word = "markera",
                        translation = "markieren",
                        wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
                        ending = "-r -de -t",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
            Arguments.of(
                ParserTestData(
                    word = "plugga (-r, -de, -t)",
                    translation = "studieren",
                    expected = Vocabulary(
                        word = "plugga",
                        translation = "studieren",
                        wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_1),
                        ending = "-r -de -t",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
            Arguments.of(
                ParserTestData(
                    word = "läsa (-er, -te, -t)",
                    translation = "studieren",
                    expected = Vocabulary(
                        word = "läsa",
                        translation = "studieren",
                        wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_2B),
                        ending = "-er -te -t",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
            Arguments.of(
                ParserTestData(
                    word = "mening (-en, -er, -erna)",
                    translation = "Satz",
                    expected = Vocabulary(
                        word = "mening",
                        translation = "Satz",
                        gender = Gender.Ultra,
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.ER),
                        ending = "-en -er -erna",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
            Arguments.of(
                ParserTestData(
                    word = "göra (gör, gjorde, gjort)",
                    translation = "machen",
                    expected = Vocabulary(
                        word = "göra",
                        translation = "machen",
                        wordGroup = WordGroup.Verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL),
                        ending = "gör gjorde gjort",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
            Arguments.of(
                ParserTestData(
                    word = "vanlig (-t, -a)",
                    translation = "gewöhnlich",
                    expected = Vocabulary(
                        word = "vanlig",
                        translation = "gewöhnlich",
                        wordGroup = WordGroup.Adjective,
                        ending = "-t -a",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
            Arguments.of(
                ParserTestData(
                    word = "generell (-t, -a)",
                    translation = "allgemein",
                    expected = Vocabulary(
                        word = "generell",
                        translation = "allgemein",
                        wordGroup = WordGroup.Adjective,
                        ending = "-t -a",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
            Arguments.of(
                ParserTestData(
                    word = "kalas (-et, -on, -ona)",
                    translation = "Fest",
                    expected = Vocabulary(
                        word = "kalas",
                        translation = "Fest",
                        gender = Gender.Neutra,
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.N),
                        ending = "-et -on -ona",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
            Arguments.of(
                ParserTestData(
                    word = "spel (-et, -, -en)",
                    translation = "Spiel",
                    expected = Vocabulary(
                        word = "spel",
                        translation = "Spiel",
                        gender = Gender.Neutra,
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.UNCHANGED_ETT),
                        ending = "-et - -en",
                        containerId = CONTAINER_ID,
                    ),
                ),
            ),
        )
    }
}
