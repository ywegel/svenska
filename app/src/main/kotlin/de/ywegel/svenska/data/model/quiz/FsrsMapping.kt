package de.ywegel.svenska.data.model.quiz

import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsCardState
import one.felsen.fsrskt.fsrs6.FsrsRating
import kotlin.time.Instant

/**
 * The phase of a [FacetCard] in the FSRS scheduler, stored by name.
 *
 * A copy of FsrsKt's `FsrsCardState`, so that a renamed constant in the library can't make stored cards unreadable.
 *
 * - [LEARNING]: new, or going through the learning steps (minutes).
 * - [REVIEW]: scheduled in whole days from the desired retention.
 * - [RELEARNING]: forgotten in review, going through the relearning steps.
 */
enum class CardPhase { LEARNING, REVIEW, RELEARNING }

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
