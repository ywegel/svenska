package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.Vocabulary
import one.felsen.fsrskt.fsrs6.FsrsRating

private const val FORMS_UNLOCK_STREAK = 3

// A quiz day starts at 4 AM, so a late session still counts towards the previous day
const val DAY_CUTOFF_HOUR = 4

val FsrsRating.isPassing: Boolean
    get() = when (this) {
        FsrsRating.GOOD, FsrsRating.EASY -> true
        FsrsRating.AGAIN, FsrsRating.HARD -> false
    }

/**
 * Whether the forms card of this vocabulary can be introduced. [Vocabulary.formsQuestion] is `null` for words without
 * quizzable forms and for forms that still need a review.
 */
fun Vocabulary.isFormsReady(svDeStreak: Int): Boolean =
    svDeStreak >= FORMS_UNLOCK_STREAK && formsQuestion() != null
