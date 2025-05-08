package de.ywegel.svenska.navigation.transitions

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import de.ywegel.svenska.navigation.transitions.TransitionDefaults.DEFAULT_TRANSITION_DURATION
import de.ywegel.svenska.ui.theme.SvenskaTheme
import kotlinx.coroutines.delay

object TransitionDefaults {
    const val DEFAULT_TRANSITION_DURATION = 340
}

/**
 * A helper composable for rendering Screen Transitions in a preview. It loops the enter and exit animation on a fullscreen box
 */
@Suppress("detekt:MagicNumber")
@VisibleForTesting
@Composable
fun AnimationPreviewContainer(enterAnimation: EnterTransition, exitAnimation: ExitTransition) {
    SvenskaTheme {
        var visible by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            while (true) {
                delay(200)
                visible = true
                delay(DEFAULT_TRANSITION_DURATION.toLong())
                delay(500)
                visible = false
                delay(DEFAULT_TRANSITION_DURATION.toLong())
            }
        }

        Surface(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = enterAnimation,
                    exit = exitAnimation,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SvenskaTheme.colors.primary),
                    )
                }
            }
        }
    }
}
