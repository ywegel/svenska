package de.ywegel.svenska.domain.quiz.compare

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.domain.quiz.FormsQuestion
import de.ywegel.svenska.domain.quiz.compare.EditStep.Match
import de.ywegel.svenska.domain.quiz.compare.EditStep.WrongLetter
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.isEqualTo

class FormsAnswerTest {
    @ParameterizedTest
    @MethodSource("correctForms")
    fun `matching form is correct`(typed: String, expected: String) {
        // Given
        val question = FormsQuestion.Verb(past = expected, supine = "sagt")

        // When
        val result = question.check(FormsQuestion.Verb(past = typed, supine = "sagt"))

        // Then
        expectThat(result.forms.first()).isEqualTo(FormVerdict.Correct)
    }

    @ParameterizedTest
    @MethodSource("wrongForms")
    fun `different form is wrong with the letters to mark`(typed: String, expected: String, alignment: List<EditStep>) {
        // Given
        val question = FormsQuestion.Verb(past = expected, supine = "sagt")

        // When
        val result = question.check(FormsQuestion.Verb(past = typed, supine = "sagt"))

        // Then
        expectThat(result.forms.first()).isEqualTo(FormVerdict.Wrong(alignment))
    }

    @ParameterizedTest
    @MethodSource("verdicts")
    fun `answer is correct only if gender and all forms are`(
        question: FormsQuestion,
        typed: FormsQuestion,
        expected: Boolean,
    ) {
        // When
        val result = question.check(typed)

        // Then
        expectThat(result.isCorrect).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("genderVerdicts")
    fun `gender is only judged for nouns`(question: FormsQuestion, typed: FormsQuestion, expected: Boolean?) {
        // When
        val result = question.check(typed)

        // Then
        expectThat(result.genderCorrect).isEqualTo(expected)
    }

    @Test
    fun `answer of another word group fails`() {
        // Given
        val question = FormsQuestion.Verb("skrev", "skrivit")

        // When / Then
        expectThrows<IllegalArgumentException> {
            question.check(FormsQuestion.Adjective("skrev", "skrivit"))
        }
    }

    companion object {
        private const val NFD_A_RING = "å"

        private val noun = FormsQuestion.Noun(Gender.Ultra, "bilar")
        private val verb = FormsQuestion.Verb("skrev", "skrivit")
        private val adjective = FormsQuestion.Adjective("stressat", "stressade")

        @JvmStatic
        fun correctForms(): List<Arguments> = listOf(
            argumentSet("equal", "sade", "sade"),
            argumentSet("first variant", "sa", "sa/sade"),
            argumentSet("second variant", "sade", "sa/sade"),
            argumentSet("different case", "Sade", "sade"),
            argumentSet("surrounding whitespace", " sade ", "sade"),
            argumentSet("decomposed å", "s${NFD_A_RING}g", "såg"),
        )

        @JvmStatic
        fun wrongForms(): List<Arguments> = listOf(
            argumentSet(
                "one wrong letter",
                "bilor",
                "bilar",
                listOf(Match("b", "b"), Match("i", "i"), Match("l", "l"), WrongLetter("o", "a"), Match("r", "r")),
            ),
            argumentSet(
                "typed case is kept",
                "Bilor",
                "bilar",
                listOf(Match("B", "b"), Match("i", "i"), Match("l", "l"), WrongLetter("o", "a"), Match("r", "r")),
            ),
            argumentSet(
                "aligned against the closest variant",
                "sode",
                "sa/sade",
                listOf(Match("s", "s"), WrongLetter("o", "a"), Match("d", "d"), Match("e", "e")),
            ),
        )

        @JvmStatic
        fun verdicts(): List<Arguments> = listOf(
            argumentSet("noun all right", noun, FormsQuestion.Noun(Gender.Ultra, "bilar"), true),
            argumentSet("noun wrong gender", noun, FormsQuestion.Noun(Gender.Neutra, "bilar"), false),
            argumentSet("noun wrong plural", noun, FormsQuestion.Noun(Gender.Ultra, "bilor"), false),
            argumentSet("verb all right", verb, FormsQuestion.Verb("skrev", "skrivit"), true),
            argumentSet("verb wrong supine", verb, FormsQuestion.Verb("skrev", "skrivt"), false),
            argumentSet("adjective all right", adjective, FormsQuestion.Adjective("stressat", "stressade"), true),
            argumentSet("adjective wrong neuter", adjective, FormsQuestion.Adjective("stressadt", "stressade"), false),
        )

        @JvmStatic
        fun genderVerdicts(): List<Arguments> = listOf(
            argumentSet("noun right gender", noun, FormsQuestion.Noun(Gender.Ultra, "bilar"), true),
            argumentSet("noun wrong gender", noun, FormsQuestion.Noun(Gender.Neutra, "bilar"), false),
            argumentSet("verb", verb, verb, null),
            argumentSet("adjective", adjective, adjective, null),
        )
    }
}
