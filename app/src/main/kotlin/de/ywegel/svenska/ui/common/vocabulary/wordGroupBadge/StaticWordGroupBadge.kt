package de.ywegel.svenska.ui.common.vocabulary.wordGroupBadge

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
fun StaticWordGroupBadgeExtended(mainWordGroup: String, subWordGroup: String? = null) {
    subWordGroup?.let {
        BaseWordGroupBadgeExtended(
            subWordGroup = subWordGroup,
            mainContent = { WordGroupText(mainWordGroup, it) },
            subContent = { WordGroupText(subWordGroup, it) },
        )
    } ?: BaseWordGroupBadge(content = { WordGroupText(mainWordGroup, it) })
}

@Composable
private fun WordGroupText(group: String, color: Color) {
    Text(
        text = group.take(1),
        style = SvenskaTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}

@Preview
@Composable
private fun ExtendedBadgePreview() {
    SvenskaTheme {
        Row(Modifier.padding(Spacings.m, Spacings.xs)) {
            StaticWordGroupBadgeExtended("Na", "Bb")
        }
    }
}

@Preview
@Composable
private fun ExtendedBadgeIncompletePreview() {
    SvenskaTheme {
        Row(Modifier.padding(Spacings.m, Spacings.xs)) {
            StaticWordGroupBadgeExtended("N", null)
        }
    }
}
