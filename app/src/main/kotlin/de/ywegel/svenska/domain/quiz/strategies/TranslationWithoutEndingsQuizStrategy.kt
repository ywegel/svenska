package de.ywegel.svenska.domain.quiz.strategies

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.quiz.QuizStrategy
import de.ywegel.svenska.domain.quiz.model.AdditionalInfo
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import kotlin.random.Random

class TranslationWithoutEndingsQuizStrategy(
    private val mode: TranslateMode,
    private val randomGenerator: () -> Boolean = { Random.nextBoolean() },
) : QuizStrategy<UserAnswer.TranslateWithoutEndingsAnswer, Boolean> {

    override fun generateQuestion(vocabulary: Vocabulary): QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer> {
        val effectiveMode = when (mode) {
            TranslateMode.SwedishToNative, TranslateMode.NativeToSwedish -> mode
            TranslateMode.Random -> if (randomGenerator()) {
                TranslateMode.SwedishToNative
            } else {
                TranslateMode.NativeToSwedish
            }
        }

        val promptData = AdditionalInfo.createFromVocabulary(
            vocabulary = vocabulary,
            translateMode = effectiveMode,
        )

        return when (effectiveMode) {
            TranslateMode.SwedishToNative -> QuizQuestion(
                vocabularyId = vocabulary.id,
                prompt = vocabulary.word,
                expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer(vocabulary.translation),
                promptData = promptData,
            )

            TranslateMode.NativeToSwedish -> QuizQuestion(
                vocabularyId = vocabulary.id,
                prompt = vocabulary.translation,
                expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer(vocabulary.word),
                promptData = promptData,
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
