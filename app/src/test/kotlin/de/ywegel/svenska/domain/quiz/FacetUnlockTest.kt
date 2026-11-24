package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.testdata.ReferenceWords
import one.felsen.fsrskt.fsrs6.FsrsRating
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.Arguments.argumentSet
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class FacetUnlockTest {
    @ParameterizedTest
    @MethodSource("ratings")
    fun `only GOOD and EASY pass`(rating: FsrsRating, expected: Boolean) {
        // When
        val result = rating.isPassing

        // Then
        expectThat(result).isEqualTo(expected)
    }

    @ParameterizedTest
    @MethodSource("formsReadiness")
    fun `forms are ready from a streak of 3 for words with forms to ask`(
        vocabulary: Vocabulary,
        svDeStreak: Int,
        expected: Boolean,
    ) {
        // When
        val result = vocabulary.isFormsReady(svDeStreak)

        // Then
        expectThat(result).isEqualTo(expected)
    }

    companion object {
        @JvmStatic
        fun ratings(): List<Arguments> = listOf(
            Arguments.of(FsrsRating.AGAIN, false),
            Arguments.of(FsrsRating.HARD, false),
            Arguments.of(FsrsRating.GOOD, true),
            Arguments.of(FsrsRating.EASY, true),
        )

        @JvmStatic
        fun formsReadiness(): List<Arguments> = listOf(
            argumentSet("streak below 3", reference("bil"), 2, false),
            argumentSet("streak of 3", reference("bil"), 3, true),
            argumentSet("no forms to ask", reference("vanlig"), 5, false),
            argumentSet("forms that need review", reference("salt"), 5, false),
        )

        private fun reference(word: String) = ReferenceWords.byWord(word).toVocabulary()
    }
}
