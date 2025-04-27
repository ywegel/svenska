package de.ywegel.svenska.domain.quiz

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer

interface QuizStrategy<A : UserAnswer, AnswerResult : Any> {
    fun generateQuestion(vocabulary: Vocabulary): QuizQuestion<A>
    fun validateAnswer(question: QuizQuestion<A>, userAnswer: A): AnswerResult
}
