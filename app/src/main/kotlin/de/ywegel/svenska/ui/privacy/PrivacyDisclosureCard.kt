package de.ywegel.svenska.ui.privacy

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun PrivacyDisclosureCard(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = SvenskaTheme.colors.secondaryContainer,
    ) {
        Column(modifier = Modifier.padding(Spacings.m)) {
            Text(
                text = stringResource(R.string.privacy_crash_collected_title),
                style = SvenskaTheme.typography.labelLarge,
                color = SvenskaTheme.colors.onSecondaryContainer,
            )
            VerticalSpacerXXS()
            Text(
                text = stringResource(R.string.privacy_crash_collected_body),
                style = SvenskaTheme.typography.bodyMedium,
                color = SvenskaTheme.colors.onSurfaceVariant,
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Spacings.s),
                color = SvenskaTheme.colors.outlineVariant,
            )
            Text(
                text = stringResource(R.string.privacy_crash_never_title),
                style = SvenskaTheme.typography.labelLarge,
                color = SvenskaTheme.colors.onSecondaryContainer,
            )
            VerticalSpacerXXS()
            Text(
                text = stringResource(R.string.privacy_crash_never_body),
                style = SvenskaTheme.typography.bodyMedium,
                color = SvenskaTheme.colors.onSurfaceVariant,
            )
        }
    }
}

@Preview
@Composable
private fun PrivacyDisclosureCardPreview() {
    SvenskaTheme {
        PrivacyDisclosureCard()
    }
}
