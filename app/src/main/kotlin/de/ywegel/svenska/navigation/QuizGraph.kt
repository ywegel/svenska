package de.ywegel.svenska.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import de.ywegel.svenska.navigation.transitions.TemporaryHierarchicalTransitionStyle

@NavGraph<SvenskaGraph>(
    defaultTransitions = TemporaryHierarchicalTransitionStyle::class,
)
annotation class QuizGraph
