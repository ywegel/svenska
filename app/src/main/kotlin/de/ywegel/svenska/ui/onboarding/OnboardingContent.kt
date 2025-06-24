@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerS
import de.ywegel.svenska.ui.onboarding.pages.BonusPage
import de.ywegel.svenska.ui.onboarding.pages.ImporterPage
import de.ywegel.svenska.ui.onboarding.pages.OnboardingPage
import de.ywegel.svenska.ui.onboarding.pages.OnboardingSimpleTextPage
import de.ywegel.svenska.ui.onboarding.pages.WordGroupPage
import de.ywegel.svenska.ui.theme.Spacings
import kotlinx.coroutines.launch

@Composable
internal fun OnboardingContent(
    innerPadding: PaddingValues,
    onOnboardingComplete: () -> Unit,
    navigateToWordGroupScreen: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPage.COUNT })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        VerticalSpacerM()
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            val page = OnboardingPage.fromIndex(page)
            OnboardingPage(page) {
                when (page) {
                    OnboardingPage.WORD_GROUP -> {
                        WordGroupPage(navigateToWordGroupScreen)
                    }

                    OnboardingPage.BONUS -> {
                        BonusPage()
                    }

                    OnboardingPage.IMPORTER -> {
                        ImporterPage()
                    }

                    else -> {
                        OnboardingSimpleTextPage(page)
                    }
                }
            }
        }

        VerticalSpacerM()

        // Page indicator
        PagerIndicator(pagerState)

        VerticalSpacerM()

        // Navigation buttons
        OnboardingNavigationButtons(pagerState = pagerState, onOnboardingComplete = onOnboardingComplete)
    }
}

@Composable
private fun OnboardingNavigationButtons(pagerState: PagerState, onOnboardingComplete: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.padding(horizontal = Spacings.m)) {
        if (pagerState.currentPage != OnboardingPage.LAST_INDEX) {
            Button(
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.onboarding_button_next))
            }

            VerticalSpacerS()
        } else {
            Button(
                onClick = onOnboardingComplete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.onboarding_button_start))
            }
        }

        AnimatedVisibility(visible = pagerState.currentPage != OnboardingPage.LAST_INDEX) {
            OutlinedButton(
                onClick = onOnboardingComplete,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.onboarding_button_skip))
            }
        }
    }
}
