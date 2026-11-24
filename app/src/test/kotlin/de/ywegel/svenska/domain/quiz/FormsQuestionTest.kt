package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.testdata.ReferenceWords
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class FormsQuestionTest {
    @ParameterizedTest
    @MethodSource("askedForms")
    fun `asks for the forms that have to be learned`(vocabulary: Vocabulary, expected: FormsQuestion) {
        // When
        val result = vocabulary.formsQuestion()

        // Then
        expectThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("nothingToAsk")
    fun `asks nothing without learnable forms`(vocabulary: Vocabulary) {
        // When
        val result = vocabulary.formsQuestion()

        // Then
        expectThat(result).isNull()
    }

    companion object {
        @JvmStatic
        fun askedForms(): List<Arguments> = listOf(
            argumentSet("en noun asks gender and plural", reference("bil"), FormsQuestion.Noun(Gender.Ultra, "bilar")),
            argumentSet(
                "ett noun asks gender and plural",
                reference("pris"),
                FormsQuestion.Noun(Gender.Neutra, "priser"),
            ),
            argumentSet("verb asks past and supine", reference("skriva"), FormsQuestion.Verb("skrev", "skrivit")),
            argumentSet("verb keeps variants as written", reference("säga"), FormsQuestion.Verb("sa/sade", "sagt")),
            argumentSet(
                "adjective off the -t -a pattern asks neuter and plural",
                reference("stressad"),
                FormsQuestion.Adjective("stressat", "stressade"),
            ),
        )

        @JvmStatic
        fun nothingToAsk(): List<Arguments> = listOf(
            argumentSet("regular adjective", reference("vanlig")),
            argumentSet("adjective following a built rule", reference("öppen")),
            argumentSet("adjective with only a plural", reference("trött")),
            argumentSet("forms that need review", reference("salt")),
            argumentSet("noun without gender", reference("bil").copy(gender = null)),
            argumentSet("noun without plural", reference("bil").copy(form2 = null)),
            argumentSet("verb without supine", reference("skriva").copy(form3 = null)),
            argumentSet("other", Vocabulary(word = "hej då", translation = "", containerId = 0)),
        )

        private fun reference(word: String) = ReferenceWords.byWord(word).toVocabulary()
    }
}
