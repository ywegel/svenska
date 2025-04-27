package de.ywegel.svenska.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import de.ywegel.svenska.data.GeneratorConstants
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun SwitchWithText(
    text: String,
    switchChecked: Boolean,
    fillTextWidth: Boolean = true,
    onSwitchChanged: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(switchChecked, onSwitchChanged)
        HorizontalSpacerS()
        Text(
            modifier = if (fillTextWidth) Modifier.weight(1f) else Modifier,
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun SwitchWithTextPreview(@PreviewParameter(SwitchWithTextPreviewProvider::class) data: String) {
    SvenskaTheme {
        SwitchWithText(text = data, switchChecked = true) { }
    }
}

private class SwitchWithTextPreviewProvider : PreviewParameterProvider<String> {
    override val values: Sequence<String> = listOf(
        "Short text",
        "Long text: ${GeneratorConstants.LONG_STRING}",
    ).asSequence()
}
