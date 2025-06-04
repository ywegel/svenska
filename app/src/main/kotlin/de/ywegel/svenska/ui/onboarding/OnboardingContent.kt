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
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerS
import de.ywegel.svenska.ui.theme.Spacings
import kotlinx.coroutines.launch

@Composable
internal fun OnboardingScreen(onOnboardingComplete: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppTextBar(
                title = stringResource(R.string.onboarding_title),
                onNavigateUp = onOnboardingComplete,
            )
        },
    ) { innerPadding ->
        OnboardingContent(
            innerPadding = innerPadding,
            onOnboardingComplete = onOnboardingComplete,
        )
    }
}

@Composable
internal fun OnboardingContent(innerPadding: PaddingValues, onOnboardingComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { OnboardingPage.COUNT })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            OnboardingPage(
                page = page,
                modifier = Modifier.fillMaxSize(),
            )
        }

        VerticalSpacerM()

        // Page indicator
        PageIndicator(
            pageCount = OnboardingPage.COUNT,
            currentPage = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
        )

        VerticalSpacerM()

        // Navigation buttons
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
}
