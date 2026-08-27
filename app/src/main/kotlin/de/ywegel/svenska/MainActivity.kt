package de.ywegel.svenska

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OnboardingScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import dagger.hilt.android.AndroidEntryPoint
import de.ywegel.svenska.navigation.AppNavigation
import de.ywegel.svenska.ui.sentryPrivacyPopUp.SentryPrivacyBottomSheet
import de.ywegel.svenska.ui.theme.SvenskaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.mainUiState.value is MainUiState.Loading
            }
        }

        enableEdgeToEdge()

        setContent {
            val state by viewModel.mainUiState.collectAsStateWithLifecycle()

            startRouteFor(state)?.let { startRoute ->
                SvenskaTheme {
                    if (shouldShowPrivacyPolicyBottomSheet(state)) {
                        SentryPrivacyBottomSheet(onAccept = viewModel::onPrivacyPolicyAccepted)
                    }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = SvenskaTheme.colors.background,
                    ) {
                        AppNavigation(startRoute = startRoute)
                    }
                }
            }
        }
    }
}

internal fun startRouteFor(state: MainUiState): Direction? = when (state) {
    MainUiState.Loading -> null
    is MainUiState.Ready -> if (state.hasCompletedOnboarding) {
        ContainerScreenDestination
    } else {
        OnboardingScreenDestination
    }
}

internal fun shouldShowPrivacyPolicyBottomSheet(state: MainUiState): Boolean =
    state is MainUiState.Ready && state.hasCompletedOnboarding && !state.isLatestPrivacyPolicyAccepted
