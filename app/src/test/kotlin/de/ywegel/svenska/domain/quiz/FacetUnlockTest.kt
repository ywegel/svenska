package de.ywegel.svenska.domain.quiz

import one.felsen.fsrskt.fsrs6.FsrsRating
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
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

    companion object {
        @JvmStatic
        fun ratings(): List<Arguments> = listOf(
            Arguments.of(FsrsRating.AGAIN, false),
            Arguments.of(FsrsRating.HARD, false),
            Arguments.of(FsrsRating.GOOD, true),
            Arguments.of(FsrsRating.EASY, true),
        )
    }
}
