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
import com.ramcosta.composedestinations.generated.destinations.OnboardingScreenDestination
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
                // Wait until the hasCompletedOnboarding Preference value was loaded
                viewModel.mainUiState.value is MainUiState.Loading
            }
        }

        enableEdgeToEdge()

        setContent {
            val state by viewModel.mainUiState.collectAsStateWithLifecycle()

            if (state is MainUiState.Ready) {
                val readyState = state as MainUiState.Ready

                SvenskaTheme {
                    if (readyState.hasCompletedOnboarding && !readyState.isLatestPrivacyPolicyAccepted) {
                        SentryPrivacyBottomSheet(onAccept = viewModel::onPrivacyPolicyAccepted)
                    }

                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = SvenskaTheme.colors.background,
                    ) {
                        val startRoute = OnboardingScreenDestination.takeIf { !readyState.hasCompletedOnboarding }

                        AppNavigation(startRoute = startRoute)
                    }
                }
            }
        }
    }
}
