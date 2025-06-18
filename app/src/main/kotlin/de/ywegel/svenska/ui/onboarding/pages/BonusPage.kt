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
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun BonusPage() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.m)) {
        OnboardingTextPage(OnboardingPage.BONUS)
        Text(
            text = stringResource(R.string.onboarding_page_content_bonus_material_extended),
            style = SvenskaTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.onboarding_word_groups_bonus),
            contentDescription = stringResource(R.string.accessibility_onboarding_page_word_group_bonus_image),
        )
    }
}
