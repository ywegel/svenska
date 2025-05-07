package de.ywegel.svenska.navigation.transitions

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.animations.NavHostAnimatedDestinationStyle
import de.ywegel.svenska.navigation.transitions.TransitionDefaults.DEFAULT_TRANSITION_DURATION

private object TemporaryHierarchicalTransitionAnimations {
    const val DEFAULT_FADE_TARGET_ALPHA = 0.8f

    // Enter animation: Slide in horizontally from the right
    val enterTransition: EnterTransition =
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(
                durationMillis = DEFAULT_TRANSITION_DURATION,
                easing = FastOutSlowInEasing,
            ),
        )

    // Exit animation: None
    val exitTransition: ExitTransition = ExitTransition.None

    // Pop Enter animation: None
    val popEnterTransition: EnterTransition = EnterTransition.None

    // Pop Exit animation: Slide out horizontally to the right
    val popExitTransition: ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(DEFAULT_TRANSITION_DURATION),
        ) + fadeOut(
            animationSpec = tween(DEFAULT_TRANSITION_DURATION),
            targetAlpha = DEFAULT_FADE_TARGET_ALPHA,
        )
}

object TemporaryHierarchicalTransitionStyle : NavHostAnimatedDestinationStyle() {
    override val enterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        TemporaryHierarchicalTransitionAnimations.enterTransition
    }

    override val exitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        TemporaryHierarchicalTransitionAnimations.exitTransition
    }

    override val popEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        TemporaryHierarchicalTransitionAnimations.popEnterTransition
    }

    override val popExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        TemporaryHierarchicalTransitionAnimations.popExitTransition
    }
}

@Preview(showBackground = true)
@Composable
fun HierarchicalTransitionPreview() {
    AnimationPreviewContainer(
        enterAnimation = TemporaryHierarchicalTransitionAnimations.enterTransition,
        exitAnimation = TemporaryHierarchicalTransitionAnimations.popExitTransition,
    )
}
