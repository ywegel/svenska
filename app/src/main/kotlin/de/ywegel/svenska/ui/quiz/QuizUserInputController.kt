package de.ywegel.svenska.ui.quiz

import de.ywegel.svenska.domain.quiz.model.UserAnswer
import kotlinx.coroutines.flow.StateFlow

interface QuizUserInputController<S : Any, A : Any> {
    val state: StateFlow<S>
    val actions: A

    fun resetState()
}

interface QuizInputState<A : UserAnswer> {
    fun toUserAnswer(): A
}
