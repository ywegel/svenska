package de.ywegel.svenska.ui.common.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun WordGroupBadge(mainWordGroup: String, color: Color = SvenskaTheme.colors.primary, surfaceAlpha: Float = 0.2f) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(
                color = color.copy(alpha = surfaceAlpha),
                shape = CircleShape,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mainWordGroup.take(1),
            style = SvenskaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Composable
fun WordGroupBadgeExtended(mainWordGroup: String, subWordGroup: String? = null) {
    subWordGroup?.let {
        WordGroupBadgeExtended(mainWordGroup = mainWordGroup, subWordGroup = subWordGroup)
    } ?: WordGroupBadge(mainWordGroup = mainWordGroup)
}

@Composable
private fun WordGroupBadgeExtended(
    mainWordGroup: String,
    subWordGroup: String,
    mainWordGroupColor: Color = SvenskaTheme.colors.tertiary,
    subWordGroupColor: Color = SvenskaTheme.colors.primary,
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
                Text(
                    text = mainWordGroup.take(1),
                    style = SvenskaTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = mainWordGroupColor,
                )
                Spacer(Modifier.width(2.dp))
            }
        }
        Box(modifier = Modifier.background(SvenskaTheme.colors.surface, shape = CircleShape)) {
            WordGroupBadge(subWordGroup, subWordGroupColor, surfaceAlpha = 0.4f)
        }
    }
}

@Preview
@Composable
private fun BadgePreview() {
    SvenskaTheme {
        Row(Modifier.padding(Spacings.m, Spacings.xs)) {
            WordGroupBadge("A")
        }
    }
}

@Preview
@Composable
private fun ExtendedBadgePreview() {
    SvenskaTheme {
        Row(Modifier.padding(Spacings.m, Spacings.xs)) {
            WordGroupBadgeExtended("Na", "Bb")
        }
    }
}

@Preview
@Composable
private fun ExtendedBadgeIncompletePreview() {
    SvenskaTheme {
        Row(Modifier.padding(Spacings.m, Spacings.xs)) {
            WordGroupBadgeExtended("N", null)
        }
    }
}
