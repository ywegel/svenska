package de.ywegel.svenska.ui.overview

/** What the vocabulary list shows of a word's quiz pool status. */
sealed interface PoolIndicator {
    data object NotInPool : PoolIndicator
    data object Queued : PoolIndicator

    /** [unlockedFacets]: 1 = SV→DE, 2 = + DE→SV, 3 = + forms. */
    data class Active(val unlockedFacets: Int) : PoolIndicator
    data object Paused : PoolIndicator
}
