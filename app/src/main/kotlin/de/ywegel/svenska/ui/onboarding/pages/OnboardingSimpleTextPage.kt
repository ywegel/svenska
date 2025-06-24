package de.ywegel.svenska.ui.onboarding.pages

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
internal fun OnboardingSimpleTextPage(page: OnboardingPage) {
    OnboardingDescriptionText(page.getContentText())
}

@Composable
internal fun OnboardingDescriptionText(textId: Int) {
    OnboardingDescriptionText(stringResource(textId))
}

@Composable
internal fun OnboardingDescriptionText(text: String) {
    Text(
        text = text,
        style = SvenskaTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
    )
}
