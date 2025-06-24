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
import de.ywegel.svenska.ui.theme.Spacings

@Composable
fun BonusPage() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.m)) {
        OnboardingDescriptionText(R.string.onboarding_page_content_bonus_material)
        OnboardingDescriptionText(R.string.onboarding_page_content_bonus_material_extended)
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(R.drawable.onboarding_word_groups_bonus),
            contentDescription = stringResource(R.string.accessibility_onboarding_page_word_group_bonus_image),
        )
    }
}
