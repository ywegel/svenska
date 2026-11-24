package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.SvDeOutcome
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.fakes.QuizCardRepositoryFake
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsDayCounter
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
    private val scheduler = FsrsScheduler(
        enableFuzzing = false,
        dayCounter = FsrsDayCounter.calendarDays(DAY_CUTOFF_HOUR, TIME_ZONE),
    )

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
    @MethodSource("svDeOutcomes")
    fun `Only SV_DE reviews change the streak, and a passing one unlocks DE_SV on the next quiz day`(
        facet: QuizFacet,
        rating: FsrsRating,
        expected: SvDeOutcome?,
    ) = runTest {
        // Given
        val repository = repositoryWithUnlocked(facet)

        // When
        submit(repository, facet, rating)

        // Then
        expectThat(repository.savedReviews.single().svDe).isEqualTo(expected)
    }

    private fun repositoryWithUnlocked(facet: QuizFacet) =
        QuizCardRepositoryFake((VOCABULARY.id to facet) to FsrsCard(due = REVIEW_AT))

    private suspend fun submit(
        repository: QuizCardRepositoryFake,
        facet: QuizFacet,
        rating: FsrsRating,
        rawAnswer: String = "lamp",
        reviewAt: Instant = REVIEW_AT,
    ) = SubmitReviewUseCase(repository, scheduler, TIME_ZONE)
        .invoke(VOCABULARY, facet, rating, rawAnswer, reviewAt, reviewDuration = 5.seconds)

    companion object {
        private val VOCABULARY = vocabulary(id = 1)
        private val TIME_ZONE = TimeZone.of("Europe/Stockholm")
        private val REVIEW_AT = LocalDateTime.parse("2026-11-24T18:00").toInstant(TIME_ZONE)
        private val NEXT_QUIZ_DAY = LocalDateTime.parse("2026-11-25T04:00").toInstant(TIME_ZONE)

        @JvmStatic
        fun svDeOutcomes() = listOf(
            Arguments.of(QuizFacet.SV_DE, FsrsRating.GOOD, SvDeOutcome.Passed(deSvDue = NEXT_QUIZ_DAY)),
            Arguments.of(QuizFacet.SV_DE, FsrsRating.AGAIN, SvDeOutcome.Failed),
            Arguments.of(QuizFacet.DE_SV, FsrsRating.GOOD, null),
            Arguments.of(QuizFacet.FORMS, FsrsRating.GOOD, null),
        )
    }
}
