package de.ywegel.svenska.data.model.quiz

import one.felsen.fsrskt.fsrs6.FsrsCard
import one.felsen.fsrskt.fsrs6.FsrsCardState
import one.felsen.fsrskt.fsrs6.FsrsRating
import kotlin.time.Instant

// Own copy of FsrsCardState, so that a renamed constant in FsrsKt can't break the stored data.
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

fun FsrsRating.toStoredRating(): Int = value

fun storedRatingToFsrs(rating: Int): FsrsRating = FsrsRating.entries.single { it.value == rating }

fun FsrsColumns.toFsrsCard() = FsrsCard(
    state = phase.toFsrs(),
    step = step,
    stability = stability,
    difficulty = difficulty,
    due = Instant.fromEpochMilliseconds(due),
    lastReview = lastReview?.let(Instant::fromEpochMilliseconds),
)

fun FsrsCard.toFsrsColumns() = FsrsColumns(
    phase = state.toCardPhase(),
    step = step,
    stability = stability,
    difficulty = difficulty,
    due = due.toEpochMilliseconds(),
    lastReview = lastReview?.toEpochMilliseconds(),
)
