package de.ywegel.svenska.ui.quiz

import de.ywegel.svenska.domain.quiz.model.UserAnswer

/**
 * A callback interface to shorten the QuizScreen parameter list
 */
interface QuizCallbacks<A : UserAnswer> {
    fun checkAnswer(input: A)
    fun nextWord()
    fun toggleFavorite(isFavorite: Boolean)
    fun returnToPreviousQuestion()
}
