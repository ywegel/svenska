package de.ywegel.svenska.ui.onboarding.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.theme.Spacings

@Composable
fun WordExtractorPage(navigateToWordExtractorExplanationScreen: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.m)) {
        OnboardingDescriptionText(R.string.onboarding_page_extractor_description)
        OnboardingDescriptionText(R.string.onboarding_page_extractor_disclaimer)
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
            Button(navigateToWordExtractorExplanationScreen) {
                Text(stringResource(R.string.onboarding_page_extractor_read_more))
            }
        }
    }
}
