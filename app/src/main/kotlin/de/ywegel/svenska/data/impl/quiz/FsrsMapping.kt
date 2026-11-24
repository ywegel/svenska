package de.ywegel.svenska.data.impl.quiz

import de.ywegel.svenska.data.db.quiz.CardPhase
import de.ywegel.svenska.data.db.quiz.FsrsColumns
import de.ywegel.svenska.data.db.quiz.ReviewLog
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.QuizFacet
import de.ywegel.svenska.data.model.quiz.formsKey
import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsCardState
import one.felsen.fsrskt.fsrs6.FsrsRating
import one.felsen.fsrskt.fsrs6.FsrsReviewLog
import kotlin.time.Instant

fun CardPhase.toFsrs(): FsrsCardState = when (this) {
    CardPhase.LEARNING -> FsrsCardState.LEARNING
    CardPhase.REVIEW -> FsrsCardState.REVIEW
    CardPhase.RELEARNING -> FsrsCardState.RELEARNING
}

fun FsrsCardState.toCardPhase(): CardPhase = when (this) {
    FsrsCardState.LEARNING -> CardPhase.LEARNING
    FsrsCardState.REVIEW -> CardPhase.REVIEW
    FsrsCardState.RELEARNING -> CardPhase.RELEARNING
}

/**
 * The rating as stored in [ReviewLog.rating]: 1 (AGAIN) to 4 (EASY), the FSRS standard values that py-fsrs and the
 * FSRS optimizer use as well.
 */
fun FsrsRating.toStoredRating(): Int = value

const val HIGHEST_FSRS_RATING = 4

/**
 * The rating of a stored value from [toStoredRating].
 *
 * @throws NoSuchElementException If [rating] is not between 1 and 4.
 */
fun storedRatingToFsrs(rating: Int): FsrsRating = FsrsRating.entries.single { it.value == rating }

fun FsrsColumns.toFsrsCard() = FsrsCard(
    state = phase.toFsrs(),
    step = step,
    stability = stability,
    difficulty = difficulty,
    due = Instant.fromEpochMilliseconds(due),
    lastReview = lastReview?.let(Instant::fromEpochMilliseconds),
)

/**
 * The columns of this card, in epoch milliseconds.
 *
 * Pass the scheduler a review time already truncated to milliseconds, so that the stored [FsrsColumns.lastReview] and
 * [FsrsColumns.due] are exactly what the scheduler computed.
 */
fun FsrsCard.toFsrsColumns() = FsrsColumns(
    phase = state.toCardPhase(),
    step = step,
    stability = stability,
    difficulty = difficulty,
    due = due.toEpochMilliseconds(),
    lastReview = lastReview?.toEpochMilliseconds(),
)

fun FsrsReviewLog.toReviewLog(vocabulary: Vocabulary, facet: QuizFacet, rawAnswer: String) = ReviewLog(
    vocabularyId = vocabulary.id,
    facet = facet,
    formsKey = vocabulary.formsKey.takeIf { facet == QuizFacet.FORMS },
    rating = rating.toStoredRating(),
    rawAnswer = rawAnswer,
    reviewAt = reviewAt.toEpochMilliseconds(),
    durationMillis = reviewDuration?.inWholeMilliseconds,
)
