package de.ywegel.svenska.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import de.ywegel.svenska.navigation.transitions.LateralTransition

/**
 * This graph only exists to group all Bonus Screens, so that we don't have to specify the style for all of them
 */
@NavGraph<SvenskaGraph>(
    defaultTransitions = LateralTransition::class,
)
annotation class BonusGraph
