package de.ywegel.svenska.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility
import de.ywegel.svenska.navigation.transitions.HierarchicalTransitionStyle

@NavGraph<SvenskaGraph>(
    visibility = CodeGenVisibility.INTERNAL,
)
annotation class SettingsNavGraph
