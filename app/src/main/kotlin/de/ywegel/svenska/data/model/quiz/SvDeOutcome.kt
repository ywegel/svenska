package de.ywegel.svenska.data.model.quiz

import kotlin.time.Instant

/**
 * What an SV_DE review changes beyond its own card. Reviews of the other facets have none.
 */
sealed interface SvDeOutcome {
    /**
     * Raises the streak and unlocks DE_SV, due at [deSvDue]. An existing DE_SV card keeps its progress.
     */
    data class Passed(val deSvDue: Instant) : SvDeOutcome

    /**
     * Sets the streak back to 0.
     */
    data object Failed : SvDeOutcome
}
