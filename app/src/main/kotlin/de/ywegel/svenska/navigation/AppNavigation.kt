package de.ywegel.svenska.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.spec.Direction

@Composable
fun AppNavigation(startRoute: Direction? = ContainerScreenDestination) {
    DestinationsNavHost(
        modifier = Modifier.fillMaxSize(),
        navGraph = NavGraphs.svenska,
        start = startRoute ?: NavGraphs.svenska.defaultStartDirection,
    )
}
