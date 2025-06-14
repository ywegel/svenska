package de.ywegel.svenska.domain.quiz.model

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup

data class QuizQuestion<A : UserAnswer>(
    val vocabularyId: Int,
    val prompt: String,
    val expectedAnswer: A,
    val promptData: QuizQuestionPromptData? = null,
)

data class QuizQuestionPromptData(
    val wordGroup: WordGroup? = null,
    val endings: String? = null,
    val gender: Gender? = null,
)
