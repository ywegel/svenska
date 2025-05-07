package de.ywegel.svenska.navigation.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import de.ywegel.svenska.navigation.transitions.TransitionDefaults.DEFAULT_TRANSITION_DURATION

/**
 * Slide Over Transition
 *
 * TODO: Evaluate if this Animation could be used somehow with the current setup
 * This can't be used with the current setup. Situation: We navigate from A -> B with this animation and then navigate
 * from B -> C with a LateralAnimation. If we now navigate back to B, we see the Screen B sliding in from the left,
 * instead of using the popEnter animation defined in the LateralAnimation
 */
object HierarchicalTransitionStyle : NavHostAnimatedDestinationStyle() {
    const val DEFAULT_FADE_TARGET_ALPHA = 0.8f

    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        // New screen slides fully in from the right
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(
                durationMillis = DEFAULT_TRANSITION_DURATION,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        // Current screen slides partially to the left and fades out
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth / 4 },
            animationSpec = tween(DEFAULT_TRANSITION_DURATION),
        ) + fadeOut(
            targetAlpha = DEFAULT_FADE_TARGET_ALPHA,
            animationSpec = tween(
                durationMillis = DEFAULT_TRANSITION_DURATION,
                easing = EaseIn,
            ),
        )
    }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        // When popping, the previous screen slides back to center and fades in
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth / 4 },
            animationSpec = tween(
                durationMillis = DEFAULT_TRANSITION_DURATION,
                easing = FastOutSlowInEasing,
            ),
        ) + fadeIn(
            initialAlpha = DEFAULT_FADE_TARGET_ALPHA,
            animationSpec = tween(
                durationMillis = DEFAULT_TRANSITION_DURATION,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        // When popping, the current screen slides fully out to the right and fades out
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(DEFAULT_TRANSITION_DURATION),
        )
    }
}
