package de.ywegel.svenska.ui.onboarding.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun ImporterPage() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.m)) {
        OnboardingTextPage(OnboardingPage.IMPORTER)
        Text(
            text = stringResource(R.string.onboarding_page_content_importer_extended),
            style = SvenskaTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        VerticalSpacerXXS()
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.onboarding_importer_open_with),
            contentDescription = stringResource(R.string.accessibility_onboarding_page_importer_open_with),
        )
    }
}
