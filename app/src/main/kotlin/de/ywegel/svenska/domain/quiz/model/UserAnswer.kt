package de.ywegel.svenska.domain.quiz.model

sealed class UserAnswer {
    data class TranslateWithoutEndingsAnswer(
        val answer: String,
    ) : UserAnswer()

    data class TranslateWithEndingsAnswer(
        val answer: String,
        val endings: String?,
    ) : UserAnswer()

    data class OnlyEndingsAnswer(
        val endings: String,
    ) : UserAnswer()
}
