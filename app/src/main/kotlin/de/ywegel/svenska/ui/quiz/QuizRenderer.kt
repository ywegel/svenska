package de.ywegel.svenska.ui.quiz

import androidx.compose.runtime.Composable
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer

interface QuizRenderer<A : UserAnswer, State : QuizInputState<A>, Actions : Any, AnswerResult : Any> {

    @Composable
    fun Prompt(question: QuizQuestion<A>)

    @Composable
    fun UserInput(question: QuizQuestion<A>, state: State, actions: Actions)

    @Composable
    fun Solution(question: QuizQuestion<A>, userAnswer: A, userAnswerResult: AnswerResult)
}
