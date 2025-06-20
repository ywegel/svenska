package de.ywegel.svenska.ui.common.vocabulary.wordGroupBadge

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun BaseWordGroupBadge(
    color: Color = SvenskaTheme.colors.primary,
    surfaceAlpha: Float = 0.2f,
    content: @Composable (Color) -> Unit,
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(
                color = color.copy(alpha = surfaceAlpha),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content(color)
    }
}

@Composable
fun BaseWordGroupBadgeExtended(
    subWordGroup: String? = null,
    mainContent: @Composable (Color) -> Unit,
    subContent: @Composable (Color) -> Unit,
) {
    subWordGroup?.let {
        BaseWordGroupBadgeExtendedImpl(
            mainContent = mainContent,
            subContent = subContent,
        )
    } ?: BaseWordGroupBadge(content = mainContent)
}

@Composable
private fun BaseWordGroupBadgeExtendedImpl(
    mainWordGroupColor: Color = SvenskaTheme.colors.tertiary,
    subWordGroupColor: Color = SvenskaTheme.colors.primary,
    mainContent: @Composable (Color) -> Unit,
    subContent: @Composable (Color) -> Unit,
) {
    Row(
        modifier = Modifier
            .height(22.dp)
            .background(
                color = mainWordGroupColor.copy(alpha = 0.3f),
                shape = CircleShape,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(16.dp, 22.dp),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Row {
                mainContent(mainWordGroupColor)
                Spacer(Modifier.width(2.dp))
            }
        }
        Box(modifier = Modifier.background(SvenskaTheme.colors.surface, shape = CircleShape)) {
            BaseWordGroupBadge(subWordGroupColor, surfaceAlpha = 0.4f, content = subContent)
        }
    }
}

@Composable
fun EmptyWordGroupBadge() {
    Box(
        modifier = Modifier
            .size(22.dp)
            .border(
                width = Dp.Hairline,
                color = SvenskaTheme.colors.onSurface,
                shape = CircleShape,
            )
            .background(
                color = Color.Transparent,
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) { }
}

@Preview
@Composable
private fun EmptyWordGroupBadgePreview() {
    SvenskaTheme {
        Row(Modifier.padding(Spacings.m, Spacings.xs)) {
            EmptyWordGroupBadge()
        }
    }
}
