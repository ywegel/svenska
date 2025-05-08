package de.ywegel.svenska.navigation.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle

abstract class SvenskaAnimatedDestinationStyle : NavHostAnimatedDestinationStyle() {
    abstract override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition

    // Do not use exitTransition and popEnterTransition. Otherwise, if we switch from a HierarchicalTransition to a Lateral
    // one, the animations get mixed
    final override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        ExitTransition.None
    }

    // Do not use exitTransition and popEnterTransition. Otherwise, if we switch from a HierarchicalTransition to a Lateral
    // one, the animations get mixed
    final override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        EnterTransition.None
    }

    abstract override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition
}
