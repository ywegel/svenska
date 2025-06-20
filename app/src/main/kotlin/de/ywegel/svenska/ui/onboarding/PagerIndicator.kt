package de.ywegel.svenska.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.IntOffset
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun PagerIndicator(pagerState: PagerState) {
    val indicatorSize = Spacings.m
    val indicatorSpacing = Spacings.xxs

    val animatedOffset by remember {
        derivedStateOf {
            (pagerState.currentPage + pagerState.currentPageOffsetFraction)
                .coerceIn(0f, (pagerState.pageCount - 1).toFloat())
        }
    }

    Box(
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .padding(bottom = Spacings.xs),
        contentAlignment = Alignment.Center,
    ) {
        Box {
            // Unselected dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(indicatorSpacing),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pagerState.pageCount) {
                    Box(
                        modifier = Modifier
                            .size(indicatorSize)
                            .clip(CircleShape)
                            .background(SvenskaTheme.colors.secondaryContainer),
                    )
                }
            }

            // Selected dot
            Box(
                modifier = Modifier
                    .offset {
                        val xOffset = (indicatorSize + indicatorSpacing) * animatedOffset
                        IntOffset(x = xOffset.roundToPx(), y = 0)
                    }
                    .size(indicatorSize)
                    .clip(CircleShape)
                    .background(SvenskaTheme.colors.primary),
            )
        }
    }
}
