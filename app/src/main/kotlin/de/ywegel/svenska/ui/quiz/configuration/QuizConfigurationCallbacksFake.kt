package de.ywegel.svenska.ui.quiz.configuration

import de.ywegel.svenska.domain.quiz.model.TranslateMode
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
object QuizConfigurationCallbacksFake : QuizConfigurationCallbacks {
    override fun quizModeChanged(mode: TranslateMode) {}
    override fun withEndingsChanged(withEndings: Boolean) {}
    override fun onlyEndingsChanged(onlyEndings: Boolean) {}
    override fun shuffleWordsChanged(shuffleWords: Boolean) {}
}
