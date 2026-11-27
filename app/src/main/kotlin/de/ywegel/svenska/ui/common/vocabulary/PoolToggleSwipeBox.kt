package de.ywegel.svenska.ui.common.vocabulary

import androidx.annotation.StringRes
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.quiz.VocabularyPoolState
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.ui.overview.VocabularyListItem
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme
import kotlin.math.roundToInt

/**
 * Swiping [content] from end to start toggles the pool state. Unlike `SwipeToDismissBox`, the content never leaves the
 * screen: It springs back after every swipe, the toggle happens when the swipe is released past [ARMED_FRACTION].
 */
@Composable
fun PoolToggleSwipeBox(
    poolState: VocabularyPoolState,
    onTogglePool: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val haptics = LocalHapticFeedback.current
    // Dragging towards the start is negative in LTR and positive in RTL.
    val towardsStart = if (LocalLayoutDirection.current == LayoutDirection.Ltr) -1f else 1f

    var width by remember { mutableIntStateOf(0) }
    var offset by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var isArmed by remember { mutableStateOf(false) }
    // Kept from the drag start, so the background does not switch to the next action while the content springs back.
    var swipedAction by remember { mutableStateOf(poolState.swipeAction()) }

    val swipedLabel = stringResource(swipedAction.label)

    val draggableState = rememberDraggableState { delta ->
        val swipedDistance = (offset + delta) * towardsStart
        offset = swipedDistance.coerceIn(0f, width.toFloat()) * towardsStart
        if (!isDragging) return@rememberDraggableState

        val armedNow = swipedDistance >= width * ARMED_FRACTION
        if (armedNow != isArmed) {
            isArmed = armedNow
            haptics.performHapticFeedback(
                if (armedNow) HapticFeedbackType.GestureThresholdActivate else HapticFeedbackType.SegmentTick,
            )
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged { width = it.width }
            .toggleAccessibilityAction(stringResource(poolState.swipeAction().label), onTogglePool)
            .draggable(
                state = draggableState,
                orientation = Orientation.Horizontal,
                onDragStarted = {
                    isDragging = true
                    swipedAction = poolState.swipeAction()
                },
                onDragStopped = { velocity ->
                    isDragging = false
                    if (isArmed) {
                        isArmed = false
                        onTogglePool()
                    }
                    // Settling through the draggable state, so a new swipe interrupts it instead of fighting it.
                    draggableState.drag {
                        animate(initialValue = offset, targetValue = 0f, initialVelocity = velocity) { value, _ ->
                            dragBy(value - offset)
                        }
                    }
                },
            ),
    ) {
        if (offset != 0f) {
            PoolSwipeBackground(
                action = swipedAction,
                label = swipedLabel,
                isArmed = isArmed,
                modifier = Modifier.matchParentSize(),
            )
        }
        Box(Modifier.offset { IntOffset(offset.roundToInt(), 0) }) {
            content()
        }
    }
}

/** Screen readers can't swipe, so they get the toggle as a custom action. */
private fun Modifier.toggleAccessibilityAction(label: String, onToggle: () -> Unit): Modifier = semantics {
    customActions = listOf(
        CustomAccessibilityAction(label) {
            onToggle()
            true
        },
    )
}

@Composable
private fun PoolSwipeBackground(
    action: PoolSwipeAction,
    label: String,
    isArmed: Boolean,
    modifier: Modifier = Modifier,
) {
    val iconScale by animateFloatAsState(if (isArmed) ARMED_ICON_SCALE else 1f, label = "PoolSwipeIconScale")
    val (containerColor, contentColor) = if (action.addsToPool) {
        SvenskaTheme.colors.primaryContainer to SvenskaTheme.colors.onPrimaryContainer
    } else {
        SvenskaTheme.colors.tertiaryContainer to SvenskaTheme.colors.onTertiaryContainer
    }

    Row(
        modifier = modifier
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

private const val ARMED_FRACTION = 0.3f
private const val ARMED_ICON_SCALE = 1.25f

@Preview(widthDp = 380)
@Composable
private fun PoolSwipePreview() {
    SvenskaTheme {
        Column(modifier = Modifier.padding(Spacings.xs), verticalArrangement = Arrangement.spacedBy(Spacings.xs)) {
            PoolSwipeAction.entries.forEachIndexed { index, action ->
                Box {
                    PoolSwipeBackground(
                        action = action,
                        label = stringResource(action.label),
                        isArmed = false,
                        modifier = Modifier.matchParentSize(),
                    )
                    Box(Modifier.offset { IntOffset(-SWIPE_PREVIEW_OFFSET, 0) }) {
                        VocabularyListItem(vocabulary = vocabulary())
                    }
                }
            }
        }
    }
}

private const val SWIPE_PREVIEW_OFFSET = 520
