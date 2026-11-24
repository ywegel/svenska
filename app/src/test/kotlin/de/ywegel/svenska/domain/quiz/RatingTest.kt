package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.db.quiz.QuizTranslationDirection
import de.ywegel.svenska.domain.quiz.compare.AnswerVerdict
import de.ywegel.svenska.domain.quiz.compare.EditStep
import de.ywegel.svenska.domain.quiz.compare.FormVerdict
import de.ywegel.svenska.domain.quiz.compare.FormsVerdict
import one.felsen.fsrskt.fsrs6.FsrsRating
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class RatingTest {
    @ParameterizedTest
    @MethodSource("answerRatings")
    fun `answer verdict is rated by how close the answer was and in which direction`(
        verdict: AnswerVerdict,
        direction: QuizTranslationDirection,
        expected: FsrsRating,
    ) {
        // When
        val result = verdict.toRating(direction)

        // Then
        expectThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("formsRatings")
    fun `forms verdict is good only if everything is right`(verdict: FormsVerdict, expected: FsrsRating) {
        // When
        val result = verdict.toRating()

        // Then
        expectThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("overrides")
    fun `each rating offers at most one override, which raises it`(
        rating: FsrsRating,
        expectedOverride: RatingOverride?,
        expectedFinalRating: FsrsRating?,
    ) {
        // When
        val override = rating.availableOverride

        // Then
        expectThat(override).isEqualTo(expectedOverride)
        expectThat(override?.rating).isEqualTo(expectedFinalRating)
    }

    companion object {
        private val wrongForm = listOf(EditStep.WrongLetter("o", "a"))

        @JvmStatic
        fun answerRatings(): List<Arguments> = listOf(
            Arguments.of(AnswerVerdict.Correct, QuizTranslationDirection.SV_DE, FsrsRating.GOOD),
            Arguments.of(AnswerVerdict.Correct, QuizTranslationDirection.DE_SV, FsrsRating.GOOD),
            Arguments.of(AnswerVerdict.Typo(wrongForm), QuizTranslationDirection.SV_DE, FsrsRating.GOOD),
            Arguments.of(AnswerVerdict.Typo(wrongForm), QuizTranslationDirection.DE_SV, FsrsRating.HARD),
            Arguments.of(AnswerVerdict.Wrong, QuizTranslationDirection.SV_DE, FsrsRating.AGAIN),
            Arguments.of(AnswerVerdict.Wrong, QuizTranslationDirection.DE_SV, FsrsRating.AGAIN),
        )

        @JvmStatic
        fun formsRatings(): List<Arguments> = listOf(
            argumentSet(
                "all right is good",
                FormsVerdict(genderCorrect = true, forms = listOf(FormVerdict.Correct)),
                FsrsRating.GOOD,
            ),
            argumentSet(
                "without gender all right is good",
                FormsVerdict(genderCorrect = null, forms = listOf(FormVerdict.Correct, FormVerdict.Correct)),
                FsrsRating.GOOD,
            ),
            argumentSet(
                "wrong gender is again",
                FormsVerdict(genderCorrect = false, forms = listOf(FormVerdict.Correct)),
                FsrsRating.AGAIN,
            ),
            argumentSet(
                "one wrong form is again",
                FormsVerdict(genderCorrect = null, forms = listOf(FormVerdict.Correct, FormVerdict.Wrong(wrongForm))),
                FsrsRating.AGAIN,
            ),
        )

        @JvmStatic
        fun overrides(): List<Arguments> = listOf(
            Arguments.of(FsrsRating.AGAIN, RatingOverride.ACTUALLY_CORRECT, FsrsRating.GOOD),
            Arguments.of(FsrsRating.HARD, RatingOverride.ACTUALLY_CORRECT, FsrsRating.GOOD),
            Arguments.of(FsrsRating.GOOD, RatingOverride.EASY, FsrsRating.EASY),
            Arguments.of(FsrsRating.EASY, null, null),
        )
    }
}
