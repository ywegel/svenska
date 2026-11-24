package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.QuizCardRepository
import de.ywegel.svenska.data.db.quiz.FacetCard
import de.ywegel.svenska.data.db.quiz.QuizPoolEntry
import de.ywegel.svenska.data.impl.quiz.toFsrsCard
import de.ywegel.svenska.data.impl.quiz.toFsrsColumns
import de.ywegel.svenska.data.impl.quiz.toReviewLog
import de.ywegel.svenska.data.model.quiz.QuizFacet
import one.felsen.fsrskt.fsrs6.FsrsRating
import one.felsen.fsrskt.fsrs6.FsrsScheduler
import javax.inject.Inject
import kotlin.time.Duration
import kotlin.time.Instant

class SubmitReviewUseCase @Inject constructor(
    private val quizCardRepository: QuizCardRepository,
    private val fsrsScheduler: FsrsScheduler,
) {
    suspend operator fun invoke(
        facetCard: FacetCard,
        poolEntry: QuizPoolEntry,
        rating: FsrsRating,
        rawAnswer: String,
        reviewAt: Instant,
        reviewDuration: Duration,
    ) {
        val (reviewedCard, fsrsLog) = fsrsScheduler.review(
            facetCard.fsrs.toFsrsCard(),
            rating,
            reviewAt,
            reviewDuration,
        )
        val log = fsrsLog.toReviewLog(facetCard, poolEntry.vocabularyId, rawAnswer)

        val updated = facetCard.withFsrs(reviewedCard.toFsrsColumns())

        val isSvDe = facetCard.facet == QuizFacet.SV_DE
        val svDeStreak = if (isSvDe) nextSvDeStreak(poolEntry.svDeStreak, rating) else null
        val unlockedDeSv = if (isSvDe && rating.isPassing) newDeSvCard(poolEntry.vocabularyId, reviewAt) else null

        quizCardRepository.saveReview(card = updated, svDeStreak, log, unlockedDeSv)
    }
}
