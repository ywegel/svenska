package de.ywegel.svenska.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.annotation.parameters.CodeGenVisibility

@NavGraph<RootGraph>(
    visibility = CodeGenVisibility.INTERNAL,
)
annotation class SettingsNavGraph
