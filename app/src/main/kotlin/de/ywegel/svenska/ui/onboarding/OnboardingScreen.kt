@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.onboarding

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.generated.destinations.WordGroupsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.navigation.SvenskaGraph
import de.ywegel.svenska.navigation.transitions.LateralTransition
import de.ywegel.svenska.ui.common.TopAppTextBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Destination<SvenskaGraph>(style = LateralTransition::class)
@Composable
fun OnboardingScreen(navigator: DestinationsNavigator) {
    val viewModel = hiltViewModel<OnboardingViewModel>()

    NavigationHandler(
        viewModel = viewModel,
        navigator = navigator,
    )

    OnboardingScreen(
        onOnboardingComplete = viewModel::completeOnboarding,
        navigateToWordGroupScreen = { navigator.navigate(WordGroupsScreenDestination) },
    )
}

@Composable
private fun OnboardingScreen(onOnboardingComplete: () -> Unit, navigateToWordGroupScreen: () -> Unit) {
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
            navigateToWordGroupScreen = navigateToWordGroupScreen,
        )
    }
}

@Composable
private fun NavigationHandler(viewModel: OnboardingViewModel, navigator: DestinationsNavigator) {
    LaunchedEffect(viewModel) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is NavigationEvent.NavigateToMain -> {
                    withContext(Dispatchers.Main.immediate) {
                        navigator.navigate(NavGraphs.svenska.defaultStartDirection) {
                            popUpTo(ContainerScreenDestination) {
                                inclusive = true
                            }
                        }
                    }
                }
            }
        }
    }
}
