@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.sentryPrivacyPopUp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.SharedUrlConstants
import de.ywegel.svenska.ui.common.FixedModalBottomSheet
import de.ywegel.svenska.ui.common.HorizontalSpacerXXS
import de.ywegel.svenska.ui.common.VerticalSpacerS
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.common.VerticalSpacerXXXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun SentryPrivacyBottomSheet(onAccept: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    FixedModalBottomSheet {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = Spacings.xl),
        ) {
            VerticalSpacerS()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(SvenskaIcons.Policy, null)
                HorizontalSpacerXXS()
                Text(
                    text = stringResource(R.string.sentryPopUp_title),
                    style = SvenskaTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
            }
            VerticalSpacerS()
            Text(
                text = stringResource(R.string.sentryPopUp_description),
                style = SvenskaTheme.typography.bodyMedium,
            )
            VerticalSpacerXS()
            TextButton(
                onClick = {
                    uriHandler.openUri(SharedUrlConstants.SVENSKA_PRIVACY_POLICY)
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.sentryPopUp_link_text))
            }
            VerticalSpacerXXXS()
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.sentryPopUp_understood))
            }
        }
    }
}

@Preview
@Composable
private fun SentryPrivacyBottomSheetPreview() {
    SvenskaTheme {
        SentryPrivacyBottomSheet({})
    }
}
