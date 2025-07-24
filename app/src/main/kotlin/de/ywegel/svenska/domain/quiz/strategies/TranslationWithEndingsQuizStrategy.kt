package de.ywegel.svenska.domain.quiz.strategies

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.quiz.QuizStrategy
import de.ywegel.svenska.domain.quiz.model.AdditionalInfo
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsResult
import kotlin.random.Random

class TranslationWithEndingsQuizStrategy(
    private val mode: TranslateMode,
    private val randomGenerator: () -> Boolean = { Random.nextBoolean() },
) : QuizStrategy<UserAnswer.TranslateWithEndingsAnswer, TranslateWithEndingsResult> {

    override fun generateQuestion(vocabulary: Vocabulary): QuizQuestion<UserAnswer.TranslateWithEndingsAnswer> {
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
            TranslateMode.SwedishToNative -> {
                QuizQuestion(
                    prompt = vocabulary.word,
                    expectedAnswer = UserAnswer.TranslateWithEndingsAnswer(
                        answer = vocabulary.translation,
                        endings = null,
                    ),
                    vocabularyId = vocabulary.id,
                    promptData = promptData,
                )
            }

            TranslateMode.NativeToSwedish -> {
                QuizQuestion(
                    prompt = vocabulary.translation,
                    expectedAnswer = UserAnswer.TranslateWithEndingsAnswer(
                        answer = vocabulary.word,
                        endings = vocabulary.ending.takeIf { it.isNotBlank() },
                    ),
                    vocabularyId = vocabulary.id,
                    promptData = promptData,
                )
            }

            TranslateMode.Random -> error(
                "TranslateMode.Random is already checked for in the effectiveMode block",
            )
        }
    }

    override fun validateAnswer(
        question: QuizQuestion<UserAnswer.TranslateWithEndingsAnswer>,
        userAnswer: UserAnswer.TranslateWithEndingsAnswer,
    ): TranslateWithEndingsResult {
        return TranslateWithEndingsResult(
            translationCorrect = question.expectedAnswer.answer.trim().equals(userAnswer.answer.trim(), true),
            endingsCorrect = ComparisonHelpers.compareEndings(
                expected = question.expectedAnswer.endings.orEmpty(),
                userInput = userAnswer.endings.orEmpty(),
            ),
        )
    }
}
