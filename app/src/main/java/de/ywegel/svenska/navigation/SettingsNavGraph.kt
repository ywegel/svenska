package de.ywegel.svenska.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootNavGraph

@RootNavGraph
@NavGraph
annotation class SettingsNavGraph(
    // Mandatory: https://composedestinations.rafaelcosta.xyz/defining-navgraphs#through-navgraph-annotations
    @Suppress("unused")
    val start: Boolean = false,
)
