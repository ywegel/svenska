package de.ywegel.svenska.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

/**
 * Enum representing onboarding pages
 */
enum class OnboardingPage {
    FIRST,
    SECOND,
    THIRD,
    FOURTH,
    ;

    companion object {

        val COUNT = entries.size

        val LAST_INDEX = COUNT - 1

        fun fromIndex(index: Int): OnboardingPage = entries.getOrElse(index) { FIRST }
    }

    fun getTitleResource(): Int = when (this) {
        FIRST -> R.string.onboarding_page_1_title
        SECOND -> R.string.onboarding_page_2_title
        THIRD -> R.string.onboarding_page_3_title
        FOURTH -> R.string.onboarding_page_4_title
    }

    fun getContentResource(): Int = when (this) {
        FIRST -> R.string.onboarding_page_1_content
        SECOND -> R.string.onboarding_page_2_content
        THIRD -> R.string.onboarding_page_3_content
        FOURTH -> R.string.onboarding_page_4_content
    }
}

@Composable
internal fun OnboardingPage(page: Int, modifier: Modifier = Modifier) {
    val onboardingPage = OnboardingPage.fromIndex(page)

    Card(modifier = modifier.padding(horizontal = Spacings.m, vertical = Spacings.xs)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacings.m),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val title = stringResource(onboardingPage.getTitleResource())
            val content = stringResource(onboardingPage.getContentResource())

            Text(
                text = title,
                style = SvenskaTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )

            VerticalSpacerM()

            Text(
                text = content,
                style = SvenskaTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }
    }
}
