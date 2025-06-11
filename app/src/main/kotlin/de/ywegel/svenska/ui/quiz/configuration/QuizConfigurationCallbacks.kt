package de.ywegel.svenska.ui.quiz.configuration

import de.ywegel.svenska.domain.quiz.model.TranslateMode

/**
 * A callback interface to shorten the QuizConfigurationScreen parameter list
 */
interface QuizConfigurationCallbacks {
    fun quizModeChanged(mode: TranslateMode)
    fun withEndingsChanged(withEndings: Boolean)
    fun onlyEndingsChanged(onlyEndings: Boolean)
}