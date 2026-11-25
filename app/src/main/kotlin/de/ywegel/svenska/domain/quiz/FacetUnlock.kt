package de.ywegel.svenska.domain.quiz

import one.felsen.fsrskt.fsrs6.FsrsRating

const val FORMS_UNLOCK_STREAK = 3

// A quiz day starts at 4 AM, so a late session still counts towards the previous day
const val DAY_CUTOFF_HOUR = 4

val FsrsRating.isPassing: Boolean
    get() = when (this) {
        FsrsRating.GOOD, FsrsRating.EASY -> true
        FsrsRating.AGAIN, FsrsRating.HARD -> false
    }
