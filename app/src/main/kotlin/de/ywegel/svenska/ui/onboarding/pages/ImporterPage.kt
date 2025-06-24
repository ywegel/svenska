package de.ywegel.svenska.ui.onboarding.pages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.theme.Spacings

@Composable
fun ImporterPage() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.m)) {
        OnboardingDescriptionText(R.string.onboarding_page_content_importer)
        OnboardingDescriptionText(R.string.onboarding_page_content_importer_extended)
        VerticalSpacerXXS()
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.onboarding_importer_open_with),
            contentDescription = stringResource(R.string.accessibility_onboarding_page_importer_open_with),
        )
    }
}
