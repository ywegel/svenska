package de.ywegel.svenska.ui.common.vocabulary

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.quiz.VocabularyPoolState
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Swiping [content] from end to start puts the word into the quiz pool or takes it out, see [VocabularyPoolState].
 * The content springs back after the swipe, as the word stays in the list. Screen readers get the same action.
 */
@Composable
fun PoolToggleSwipeBox(
    poolState: VocabularyPoolState,
    onTogglePool: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val swipeState = rememberSwipeToDismissBoxState(positionalThreshold = { width -> width * SWIPE_THRESHOLD })
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current
    val isArmed = swipeState.targetValue == SwipeToDismissBoxValue.EndToStart

    // The toggle changes poolState while the content springs back. Keep showing the action that was swiped until the
    // content is back in place, otherwise the background switches to the opposite action mid animation.
    var shownAction by remember { mutableStateOf(poolState.swipeAction()) }
    val action = poolState.swipeAction()
    LaunchedEffect(action) {
        snapshotFlow { swipeState.dismissDirection }.first { it == SwipeToDismissBoxValue.Settled }
        shownAction = action
    }
    val label = stringResource(shownAction.label)

    // SwipeToDismissBox calls onDismiss from an effect keyed on the lambda. A new lambda on every recomposition would
    // restart that effect while the box is still dismissed, toggling again on each poolState change.
    val currentOnTogglePool by rememberUpdatedState(onTogglePool)
    val onDismiss: (SwipeToDismissBoxValue) -> Unit = remember(swipeState, scope) {
        {
            currentOnTogglePool()
            scope.launch { swipeState.reset() }
        }
    }

    // Tick when the swipe crosses the threshold, and again when it is dragged back below it, so cancelling is felt too.
    // Springing back after a dismiss also drops below the threshold, settledValue tells the two apart.
    LaunchedEffect(swipeState, haptics) {
        snapshotFlow { swipeState.targetValue == SwipeToDismissBoxValue.EndToStart }
            .drop(1)
            .collect { armed ->
                when {
                    armed -> haptics.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    swipeState.settledValue == SwipeToDismissBoxValue.Settled -> {
                        haptics.performHapticFeedback(HapticFeedbackType.SegmentTick)
                    }
                }
            }
    }

    SwipeToDismissBox(
        state = swipeState,
        modifier = modifier.semantics {
            customActions = listOf(CustomAccessibilityAction(label) { currentOnTogglePool(); true })
        },
        enableDismissFromStartToEnd = false,
        onDismiss = onDismiss,
        backgroundContent = { PoolSwipeBackground(action = shownAction, label = label, isArmed = isArmed) },
        content = content,
    )
}

@Composable
private fun PoolSwipeBackground(action: PoolSwipeAction, label: String, isArmed: Boolean) {
    val iconScale by animateFloatAsState(if (isArmed) ARMED_ICON_SCALE else 1f, label = "PoolSwipeIconScale")
    // Dynamic colors derive secondary from the primary hue, so the two containers look alike. Tertiary has its own hue.
    val (containerColor, contentColor) = if (action.addsToPool) {
        SvenskaTheme.colors.primaryContainer to SvenskaTheme.colors.onPrimaryContainer
    } else {
        SvenskaTheme.colors.tertiaryContainer to SvenskaTheme.colors.onTertiaryContainer
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(Spacings.xs))
            .background(containerColor)
            .padding(horizontal = Spacings.m),
        horizontalArrangement = Arrangement.spacedBy(Spacings.xs, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = SvenskaTheme.typography.labelLarge, color = contentColor)
        Icon(
            imageVector = action.icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.scale(iconScale),
        )
    }
}

private enum class PoolSwipeAction(@StringRes val label: Int, val icon: ImageVector, val addsToPool: Boolean) {
    Queue(R.string.vocabulary_list_swipe_queue, Icons.AutoMirrored.Rounded.PlaylistAdd, addsToPool = true),
    Resume(R.string.vocabulary_list_swipe_resume, Icons.Rounded.PlayArrow, addsToPool = true),
    Remove(R.string.vocabulary_list_swipe_remove, Icons.Rounded.PlaylistRemove, addsToPool = false),
    Pause(R.string.vocabulary_list_swipe_pause, Icons.Rounded.Pause, addsToPool = false),
}

private fun VocabularyPoolState.swipeAction(): PoolSwipeAction = when (this) {
    VocabularyPoolState.NotInPool -> PoolSwipeAction.Queue
    VocabularyPoolState.Paused -> PoolSwipeAction.Resume
    VocabularyPoolState.Queued -> PoolSwipeAction.Remove
    is VocabularyPoolState.Active -> PoolSwipeAction.Pause
}

private const val SWIPE_THRESHOLD = 0.4f
private const val ARMED_ICON_SCALE = 1.25f

@Preview(widthDp = 380)
@Composable
private fun PoolSwipeBackgroundPreview() {
    SvenskaTheme {
        Column(modifier = Modifier.padding(Spacings.xs), verticalArrangement = Arrangement.spacedBy(Spacings.xs)) {
            PoolSwipeAction.entries.forEachIndexed { index, action ->
                Box(Modifier.height(PREVIEW_ROW_HEIGHT)) {
                    PoolSwipeBackground(action, stringResource(action.label), isArmed = index % 2 == 0)
                }
            }
        }
    }
}

private val PREVIEW_ROW_HEIGHT = 56.dp
