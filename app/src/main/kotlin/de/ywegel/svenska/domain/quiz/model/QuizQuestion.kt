package de.ywegel.svenska.domain.quiz.model

data class QuizQuestion<A : UserAnswer>(
    val vocabularyId: Int,
    val prompt: String,
    val expectedAnswer: A,
)
