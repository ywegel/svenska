package de.ywegel.svenska.ui.common

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.ywegel.svenska.data.GeneratorConstants
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun ClickableText(title: String, description: String? = null, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = ripple(bounded = true),
                interactionSource = remember { MutableInteractionSource() },
            )
            .padding(horizontal = Spacings.m, vertical = Spacings.s),
    ) {
        Column {
            Text(
                text = title,
                style = SvenskaTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (description != null) {
                VerticalSpacerXXXS()
                Text(
                    text = description,
                    style = SvenskaTheme.typography.bodyMedium,
                    color = SvenskaTheme.colors.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ClickableTextPreview(@PreviewParameter(ClickableTextPreviewProvider::class) data: ClickableTextPreviewData) {
    SvenskaTheme {
        ClickableText(
            title = data.title,
            description = data.description,
            onClick = {},
        )
    }
}

@VisibleForTesting
private class ClickableTextPreviewProvider : PreviewParameterProvider<ClickableTextPreviewData> {
    override val values: Sequence<ClickableTextPreviewData> = listOf(
        ClickableTextPreviewData(title = "Short title"),
        ClickableTextPreviewData(title = "Long title: ${GeneratorConstants.LONG_STRING}"),
        ClickableTextPreviewData(title = "Short title", description = "Short description"),
        ClickableTextPreviewData(
            title = "Short title",
            description = "Long description: ${GeneratorConstants.LONG_STRING}",
        ),
        ClickableTextPreviewData(
            title = "Long title: ${GeneratorConstants.LONG_STRING}",
            description = "Long description: ${GeneratorConstants.LONG_STRING}",
        ),
    ).asSequence()
}

data class ClickableTextPreviewData(
    val title: String,
    val description: String? = null,
)
