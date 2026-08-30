package de.ywegel.svenska.ui.privacy

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.VerticalSpacerL
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerXXL
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun PrivacyCrashReportingStep(onDecision: (enabled: Boolean) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Text(
            text = stringResource(R.string.privacy_crash_title),
            style = SvenskaTheme.typography.headlineMedium,
            color = SvenskaTheme.colors.onSurface,
        )
        VerticalSpacerM()
        Text(
            text = stringResource(R.string.privacy_crash_body),
            style = SvenskaTheme.typography.bodyLarge,
            color = SvenskaTheme.colors.onSurfaceVariant,
        )
        VerticalSpacerL()
        PrivacyDisclosureCard(modifier = Modifier.fillMaxWidth())
        VerticalSpacerXXL()
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacings.xs),
        ) {
            OutlinedButton(
                onClick = { onDecision(false) },
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.privacy_crash_decline),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Button(
                onClick = { onDecision(true) },
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.privacy_crash_accept),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        VerticalSpacerM()
        Text(
            text = stringResource(R.string.privacy_crash_footnote),
            style = SvenskaTheme.typography.bodySmall,
            color = SvenskaTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview
@Composable
private fun PrivacyCrashReportingStepPreview() {
    SvenskaTheme {
        PrivacyCrashReportingStep(onDecision = {})
    }
}
