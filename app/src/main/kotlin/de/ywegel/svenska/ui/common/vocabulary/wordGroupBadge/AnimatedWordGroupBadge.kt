package de.ywegel.svenska.ui.common.vocabulary.wordGroupBadge

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun AnimatedWordGroupBadgeExtended(mainWordGroup: String, subWordGroup: String? = null) {
    subWordGroup?.let {
        BaseWordGroupBadgeExtended(
            subWordGroup = subWordGroup,
            mainContent = { AnimatedWordGroupText(mainWordGroup, it) },
            subContent = { AnimatedWordGroupText(subWordGroup, it) },
        )
    } ?: BaseWordGroupBadge {
        AnimatedWordGroupText(mainWordGroup, it)
    }
}

@Composable
private fun AnimatedWordGroupText(group: String, color: Color) {
    AnimatedContent(
        targetState = group.take(1),
        transitionSpec = {
            slideInVertically { height -> -height } + fadeIn() togetherWith
                slideOutVertically { height -> height } + fadeOut()
        },
    ) { letter ->
        Text(
            text = letter,
            style = SvenskaTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}

@Preview
@Composable
private fun ExtendedBadgePreview() {
    SvenskaTheme {
        Row(Modifier.padding(Spacings.m, Spacings.xs)) {
            AnimatedWordGroupBadgeExtended("Na", "Bb")
        }
    }
}

@Preview
@Composable
private fun ExtendedBadgeIncompletePreview() {
    SvenskaTheme {
        Row(Modifier.padding(Spacings.m, Spacings.xs)) {
            AnimatedWordGroupBadgeExtended("N", null)
        }
    }
}
