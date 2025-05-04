package de.ywegel.svenska.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class VocabularyTest {

    @Disabled // TODO: Needs to be fixed with the annotation fix
    @ParameterizedTest
    @MethodSource("provideValidAnnotationsTestData")
    fun `extractAnnotations with valid annotations`(
        testAnnotatedWord: String,
        expectedHighlights: List<Int>,
    ) {
        val result = Vocabulary.fromAnnotatedWord(
            wordWithHighlights = testAnnotatedWord,
            translation = "",
            gender = Gender.defaultIfEmpty,
            wordGroup = WordGroup.default,
            ending = "",
            notes = "",
            containerId = 0,
        )

        assertThat(result.word).isEqualTo(testAnnotatedWord.filterNot { it == '*' })
        assertThat(result.wordHighlights).isEqualTo(expectedHighlights)
    }

    companion object {

        @JvmStatic
        fun provideValidAnnotationsTestData(): Stream<Arguments> = Stream.of(
            Arguments.of("l*a*mpa", listOf(1, 2)),
            Arguments.of("a*b*c", listOf(1, 2)),
            Arguments.of("*b*c", listOf(0, 1)),
            Arguments.of("a*b*", listOf(1, 2)),
            Arguments.of("*b*", listOf(0, 1)),
            Arguments.of("**", listOf(0, 0)),
            Arguments.of("a*bc*d", listOf(1, 3)),
            Arguments.of("*bc*d", listOf(0, 2)),
            Arguments.of("a*bc*", listOf(1, 3)),
            Arguments.of("a**d", listOf(1, 1)),
            Arguments.of("*a*b*cd*e", listOf(0, 1, 2, 4)),
            Arguments.of("*a*b*cd*e*f*g*e*", listOf(0, 1, 2, 4, 5, 6, 7, 8)),
        )
    }
}
