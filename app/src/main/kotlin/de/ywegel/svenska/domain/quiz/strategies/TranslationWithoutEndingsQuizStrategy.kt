package de.ywegel.svenska.domain.quiz.strategies

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.quiz.QuizStrategy
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.QuizQuestionPromptData
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import kotlin.random.Random

class TranslationWithoutEndingsQuizStrategy(
    private val mode: TranslateMode,
    private val randomGenerator: () -> Boolean = { Random.nextBoolean() },
) : QuizStrategy<UserAnswer.TranslateWithoutEndingsAnswer, Boolean> {

    override fun generateQuestion(vocabulary: Vocabulary): QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer> {
        val effectiveMode = when (mode) {
            TranslateMode.Swedish, TranslateMode.Native -> mode
            TranslateMode.Random -> if (randomGenerator()) TranslateMode.Swedish else TranslateMode.Native
        }

        return when (effectiveMode) {
            TranslateMode.Swedish -> QuizQuestion(
                vocabularyId = vocabulary.id,
                prompt = vocabulary.word,
                expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer(vocabulary.translation),
                promptData = QuizQuestionPromptData(
                    wordGroup = vocabulary.wordGroup,
                    endings = vocabulary.ending,
                    gender = vocabulary.gender,
                ),
            )

            TranslateMode.Native -> QuizQuestion(
                vocabularyId = vocabulary.id,
                prompt = vocabulary.translation,
                expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer(vocabulary.word),
                promptData = null,
            )

            TranslateMode.Random -> error(
                "TranslateMode.Random is already checked for in the effectiveMode block",
            )
        }
    }

    override fun validateAnswer(
        question: QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>,
        userAnswer: UserAnswer.TranslateWithoutEndingsAnswer,
    ): Boolean {
        return question.expectedAnswer.answer.trim().equals(userAnswer.answer.trim(), true)
    }
}
