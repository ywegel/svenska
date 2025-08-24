package de.ywegel.svenska.data

import de.ywegel.svenska.assertVocabularyEqualsIgnoringTimestamps
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.wordImporter.WordParserImpl
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

    data class ParserTestData(
        val word: String,
        val translation: String,
        val expected: Vocabulary,
    )

    companion object {
        private const val CONTAINER_ID = 99

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
