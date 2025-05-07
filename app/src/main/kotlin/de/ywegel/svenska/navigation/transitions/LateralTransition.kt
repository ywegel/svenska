package de.ywegel.svenska.navigation.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import de.ywegel.svenska.navigation.transitions.TransitionDefaults.DEFAULT_TRANSITION_DURATION

private object LateralTransitionAnimations {
    private const val DEFAULT_INITIAL_FADE_IN_ALPHA = 0.95f
    private const val DEFAULT_EXIT_TRANSITION_DURATION = 220
    private const val DEFAULT_INITIAL_SCALE_SIZE = 0.90f

    // Entry animation: Slide up, Fade in, Scale in
    val enterTransition: EnterTransition =
        slideInVertically(
            initialOffsetY = { fullHeight -> fullHeight * 1 / 5 },
            animationSpec = tween(
                durationMillis = DEFAULT_TRANSITION_DURATION,
                easing = EaseOut,
            ),
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = DEFAULT_TRANSITION_DURATION,
                easing = EaseOut,
            ),
            initialAlpha = DEFAULT_INITIAL_FADE_IN_ALPHA,
        ) + scaleIn(
            initialScale = DEFAULT_INITIAL_SCALE_SIZE,
            animationSpec = tween(DEFAULT_TRANSITION_DURATION),
        )

    // Exit animation: None
    val exitTransition: ExitTransition = ExitTransition.None

    // Pop Enter animation: Simple Fade In
    val popEnterTransition: EnterTransition = EnterTransition.None

    // Pop Exit animation: Slide down, Fade out
    val popExitTransition: ExitTransition =
        slideOutVertically(
            targetOffsetY = { fullHeight -> fullHeight * 1 / 5 },
            animationSpec = tween(
                durationMillis = DEFAULT_EXIT_TRANSITION_DURATION,
                easing = EaseIn,
            ),
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = DEFAULT_EXIT_TRANSITION_DURATION,
                easing = EaseInOut,
            ),
        )
}

object LateralTransition : NavHostAnimatedDestinationStyle() {
    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        LateralTransitionAnimations.enterTransition
    }

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        LateralTransitionAnimations.exitTransition
    }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        LateralTransitionAnimations.popEnterTransition
    }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        LateralTransitionAnimations.popExitTransition
    }
}

@Preview(showBackground = true)
@Composable
fun LateralTransitionPreview() {
    AnimationPreviewContainer(
        enterAnimation = LateralTransitionAnimations.enterTransition,
        exitAnimation = LateralTransitionAnimations.popExitTransition,
    )
}
