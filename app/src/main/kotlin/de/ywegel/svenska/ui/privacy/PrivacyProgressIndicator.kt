package de.ywegel.svenska.ui.privacy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.ui.theme.SvenskaTheme

private val DotWidth = 24.dp
private val DotHeight = 4.dp
private val DotShape = RoundedCornerShape(2.dp)

@Composable
fun PrivacyProgressIndicator(activeStepIndex: Int, modifier: Modifier = Modifier, totalSteps: Int = 2) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(totalSteps) { index ->
            val color = if (index == activeStepIndex) {
                SvenskaTheme.colors.primary
            } else {
                SvenskaTheme.colors.outlineVariant
            }
            Box(
                modifier = Modifier
                    .width(DotWidth)
                    .height(DotHeight)
                    .background(color = color, shape = DotShape),
            )
        }
    }
}

@Preview
@Composable
private fun PrivacyProgressIndicatorStep1Preview() {
    SvenskaTheme {
        PrivacyProgressIndicator(activeStepIndex = 0)
    }
}

@Preview
@Composable
private fun PrivacyProgressIndicatorStep2Preview() {
    SvenskaTheme {
        PrivacyProgressIndicator(activeStepIndex = 1)
    }
}
