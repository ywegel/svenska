package de.ywegel.svenska.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.navigation.navGraph
import com.ramcosta.composedestinations.spec.Direction
import de.ywegel.svenska.ui.settings.SettingsViewModel

@Composable
fun AppNavigation(startRoute: Direction? = ContainerScreenDestination) {
    DestinationsNavHost(
        modifier = Modifier.fillMaxSize(),
        navGraph = NavGraphs.svenska,
        start = startRoute ?: NavGraphs.svenska.defaultStartDirection,
        dependenciesContainerBuilder = {
            // 👇 To tie SettingsViewModel to "settings" nested navigation graph,
            // making it available to all screens that belong to it
            navGraph(NavGraphs.settings) {
                val parentEntry = remember(navBackStackEntry) {
                    navController.getBackStackEntry(NavGraphs.settings)
                }
                dependency(hiltViewModel<SettingsViewModel>(parentEntry))
            }
        },
    )
}
