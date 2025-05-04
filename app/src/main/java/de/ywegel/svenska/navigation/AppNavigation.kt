package de.ywegel.svenska.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs

@Composable
fun AppNavigation() {
    DestinationsNavHost(
        modifier = Modifier.fillMaxSize(),
        navGraph = NavGraphs.root,
    )
}
