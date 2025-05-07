package de.ywegel.svenska.navigation

import com.ramcosta.composedestinations.annotation.NavHostGraph
import de.ywegel.svenska.navigation.transitions.TemporaryHierarchicalTransitionStyle

@NavHostGraph(
    defaultTransitions = TemporaryHierarchicalTransitionStyle::class,
)
annotation class SvenskaGraph
