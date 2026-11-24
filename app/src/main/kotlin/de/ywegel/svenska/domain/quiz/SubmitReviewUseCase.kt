package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.QuizCardRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizFacet
import kotlinx.datetime.TimeZone
import one.felsen.fsrskt.fsrs6.FsrsRating
import one.felsen.fsrskt.fsrs6.FsrsScheduler
import one.felsen.fsrskt.helper.DateTimeHelper.startOfNextDay
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Instant

/**
 * Reviews a facet of a vocabulary with its final [FsrsRating] and saves the result. A passing SV_DE review raises the
 * streak and unlocks DE_SV, due at the start of the next quiz day. A locked or deleted card is not reviewed.
 */
class SubmitReviewUseCase @Inject constructor(
    private val quizCardRepository: QuizCardRepository,
    private val fsrsScheduler: FsrsScheduler,
    private val timeZone: TimeZone,
) {
    suspend operator fun invoke(
        vocabulary: Vocabulary,
        facet: QuizFacet,
        rating: FsrsRating,
        rawAnswer: String,
        reviewAt: Instant,
        reviewDuration: Duration,
    ) {
        val reviewAtMillis = Instant.fromEpochMilliseconds(reviewAt.toEpochMilliseconds())
        val card = quizCardRepository.getCard(vocabulary, facet) ?: return
        val (reviewedCard, log) = fsrsScheduler.review(card, rating, reviewAtMillis, reviewDuration)

        val isSvDe = facet == QuizFacet.SV_DE
        quizCardRepository.saveReview(
            vocabulary = vocabulary,
            facet = facet,
            card = reviewedCard,
            log = log,
            rawAnswer = rawAnswer,
            passedSvDe = rating.isPassing.takeIf { isSvDe },
            unlockDeSvDue = reviewAtMillis.startOfNextDay(DAY_CUTOFF_HOUR, timeZone).takeIf { isSvDe && rating.isPassing },
        )
    }
}
