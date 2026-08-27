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
import de.ywegel.svenska.ui.theme.SvenskaTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen().apply {
            setKeepOnScreenCondition {
                viewModel.onboardingState.value is OnboardingState.Loading
            }
        }

        enableEdgeToEdge()

        setContent {
            val onboardingState by viewModel.onboardingState.collectAsStateWithLifecycle()

            startRouteFor(onboardingState)?.let { startRoute ->
                SvenskaTheme {
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

internal fun startRouteFor(onboardingState: OnboardingState): Direction? = when (onboardingState) {
    OnboardingState.Loading -> null
    OnboardingState.NotCompleted -> OnboardingScreenDestination
    OnboardingState.Completed -> ContainerScreenDestination
}
