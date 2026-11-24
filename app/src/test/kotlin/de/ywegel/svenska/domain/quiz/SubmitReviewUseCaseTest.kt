package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.fakes.QuizCardRepositoryFake
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsRating
import one.felsen.fsrskt.fsrs6.FsrsScheduler
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class SubmitReviewUseCaseTest {

    @Test
    fun `Reviewing a card saves the rescheduled card with its log and the raw answer`() = runTest {
        // Given
        val repository = repositoryWithUnlocked(QuizFacet.SV_DE)

        // When
        submit(repository, QuizFacet.SV_DE, FsrsRating.GOOD, rawAnswer = "lamp")

        // Then
        val saved = repository.savedReviews.single()
        expectThat(saved.rawAnswer).isEqualTo("lamp")
        expectThat(saved.log.rating).isEqualTo(FsrsRating.GOOD)
        expectThat(saved.card.lastReview).isEqualTo(REVIEW_AT)
        expectThat(saved.card.due).isGreaterThan(REVIEW_AT)
    }

    @Test
    fun `A locked card is not reviewed`() = runTest {
        // Given
        val repository = QuizCardRepositoryFake()

        // When
        submit(repository, QuizFacet.DE_SV, FsrsRating.GOOD)

        // Then
        expectThat(repository.savedReviews).isEmpty()
    }

    @ParameterizedTest
    @MethodSource("streakCases")
    fun `Only SV_DE reviews change the streak`(facet: QuizFacet, rating: FsrsRating, passedSvDe: Boolean?) = runTest {
        // Given
        val repository = repositoryWithUnlocked(facet)

        // When
        submit(repository, facet, rating)

        // Then
        expectThat(repository.savedReviews.single().passedSvDe).isEqualTo(passedSvDe)
    }

    @ParameterizedTest
    @MethodSource("unlockCases")
    fun `Only a passing SV_DE review unlocks DE_SV`(facet: QuizFacet, rating: FsrsRating, unlocks: Boolean) = runTest {
        // Given
        val repository = repositoryWithUnlocked(facet)

        // When
        submit(repository, facet, rating)

        // Then
        expectThat(repository.savedReviews.single().unlockDeSvDue != null).isEqualTo(unlocks)
    }

    private fun repositoryWithUnlocked(facet: QuizFacet) =
        QuizCardRepositoryFake((VOCABULARY.id to facet) to FsrsCard(due = REVIEW_AT))

    private suspend fun submit(
        repository: QuizCardRepositoryFake,
        facet: QuizFacet,
        rating: FsrsRating,
        rawAnswer: String = "lamp",
        reviewAt: Instant = REVIEW_AT,
    ) = SubmitReviewUseCase(repository, FsrsScheduler(enableFuzzing = false), TIME_ZONE)
        .invoke(VOCABULARY, facet, rating, rawAnswer, reviewAt, reviewDuration = 5.seconds)

    companion object {
        private val VOCABULARY = vocabulary(id = 1)
        private val TIME_ZONE = TimeZone.of("Europe/Stockholm")
        private val REVIEW_AT = LocalDateTime.parse("2026-11-24T18:00").toInstant(TIME_ZONE)

        @JvmStatic
        fun streakCases() = listOf(
            Arguments.of(QuizFacet.SV_DE, FsrsRating.GOOD, true),
            Arguments.of(QuizFacet.SV_DE, FsrsRating.AGAIN, false),
            Arguments.of(QuizFacet.DE_SV, FsrsRating.GOOD, null),
            Arguments.of(QuizFacet.FORMS, FsrsRating.GOOD, null),
        )

        @JvmStatic
        fun unlockCases() = listOf(
            Arguments.of(QuizFacet.SV_DE, FsrsRating.GOOD, true),
            Arguments.of(QuizFacet.SV_DE, FsrsRating.AGAIN, false),
            Arguments.of(QuizFacet.DE_SV, FsrsRating.GOOD, false),
            Arguments.of(QuizFacet.FORMS, FsrsRating.GOOD, false),
        )

    }
}
