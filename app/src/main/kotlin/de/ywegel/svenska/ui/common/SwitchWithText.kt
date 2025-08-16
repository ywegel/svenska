package de.ywegel.svenska.ui.common

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.ywegel.svenska.data.GeneratorConstants
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun SwitchWithText(
    title: String,
    description: String? = null,
    checked: Boolean,
    fillTextWidth: Boolean = true,
    onCheckedChange: (Boolean) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true),
                onClick = { onCheckedChange(!checked) },
            )
            .padding(horizontal = Spacings.m, vertical = Spacings.s),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = if (fillTextWidth) Modifier.weight(1f) else Modifier,
            ) {
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

            HorizontalSpacerS()

            Switch(
                checked = checked,
                onCheckedChange = { onCheckedChange(!checked) },
            )
        }
    }
}

@Preview
@Composable
private fun SwitchWithTextPreview(@PreviewParameter(SwitchWithTextPreviewProvider::class) data: SwitchTextPreviewData) {
    SvenskaTheme {
        SwitchWithText(title = data.title, description = data.description, checked = true) { }
    }
}

@VisibleForTesting
private class SwitchWithTextPreviewProvider : PreviewParameterProvider<SwitchTextPreviewData> {
    override val values: Sequence<SwitchTextPreviewData> = listOf(
        SwitchTextPreviewData(title = "Short title", description = null),
        SwitchTextPreviewData(title = "Long text: ${GeneratorConstants.LONG_STRING}", description = null),
        SwitchTextPreviewData(title = "Short title", description = "Short description"),
        SwitchTextPreviewData(
            title = "Short title",
            description = "Long description:  ${GeneratorConstants.LONG_STRING}",
        ),
        SwitchTextPreviewData(
            title = "Long text: ${GeneratorConstants.LONG_STRING}",
            description = "Long description:  ${GeneratorConstants.LONG_STRING}",
        ),
    ).asSequence()
}

data class SwitchTextPreviewData(
    val title: String,
    val description: String?,
)
