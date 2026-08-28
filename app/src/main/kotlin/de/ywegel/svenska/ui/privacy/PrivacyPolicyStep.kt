package de.ywegel.svenska.ui.privacy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.SharedUrlConstants
import de.ywegel.svenska.ui.common.HorizontalSpacerXXS
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerXXL
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun PrivacyPolicyStep(onContinueClicked: () -> Unit, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current
    val privacyPolicyUrl = SharedUrlConstants.SVENSKA_PRIVACY_POLICY

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacings.xs),
        ) {
            Icon(
                imageVector = SvenskaIcons.Policy,
                contentDescription = null,
                tint = SvenskaTheme.colors.primary,
                modifier = Modifier.size(28.dp),
            )
            Text(
                text = stringResource(R.string.privacy_policy_title),
                style = SvenskaTheme.typography.headlineMedium,
                color = SvenskaTheme.colors.onSurface,
            )
        }
        VerticalSpacerM()
        Text(
            text = stringResource(R.string.privacy_policy_body),
            style = SvenskaTheme.typography.bodyLarge,
            color = SvenskaTheme.colors.onSurfaceVariant,
        )
        VerticalSpacerM()
        ReadPolicyLink(onClick = { uriHandler.openUri(privacyPolicyUrl) })
        VerticalSpacerXXL()
        Button(
            onClick = onContinueClicked,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.privacy_policy_continue))
        }
        VerticalSpacerM()
        Text(
            text = stringResource(R.string.privacy_policy_footnote),
            style = SvenskaTheme.typography.bodySmall,
            color = SvenskaTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun ReadPolicyLink(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = ripple(),
            onClick = onClick,
        ),
    ) {
        Text(
            text = stringResource(R.string.privacy_policy_link),
            style = SvenskaTheme.typography.labelLarge,
            color = SvenskaTheme.colors.primary,
        )
        HorizontalSpacerXXS()
        Icon(
            imageVector = SvenskaIcons.OpenInNew,
            contentDescription = null,
            tint = SvenskaTheme.colors.primary,
            modifier = Modifier.size(Spacings.m),
        )
    }
}

@Preview
@Composable
private fun PrivacyPolicyStepPreview() {
    SvenskaTheme {
        PrivacyPolicyStep(onContinueClicked = {})
    }
}
