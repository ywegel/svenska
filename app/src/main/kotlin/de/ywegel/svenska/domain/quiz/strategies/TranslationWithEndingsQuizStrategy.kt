package de.ywegel.svenska.domain.quiz.strategies

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.domain.quiz.QuizStrategy
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.QuizQuestionPromptData
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
            TranslateMode.Swedish, TranslateMode.Native -> mode
            TranslateMode.Random -> if (randomGenerator()) TranslateMode.Swedish else TranslateMode.Native
        }

        return when (effectiveMode) {
            TranslateMode.Swedish -> {
                QuizQuestion(
                    prompt = vocabulary.word,
                    expectedAnswer = UserAnswer.TranslateWithEndingsAnswer(
                        answer = vocabulary.translation,
                        endings = null,
                    ),
                    vocabularyId = vocabulary.id,
                    promptData = QuizQuestionPromptData(
                        wordGroup = vocabulary.wordGroup,
                        endings = vocabulary.ending.takeIf { it.isNotBlank() },
                        gender = vocabulary.gender,
                    ),
                )
            }

            TranslateMode.Native -> {
                QuizQuestion(
                    prompt = vocabulary.translation,
                    expectedAnswer = UserAnswer.TranslateWithEndingsAnswer(
                        answer = vocabulary.word,
                        endings = vocabulary.ending.takeIf { it.isNotBlank() },
                    ),
                    vocabularyId = vocabulary.id,
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
            endingsCorrect = compareEndings(question.expectedAnswer.endings.orEmpty(), userAnswer.endings.orEmpty()),
        )
    }

    /**
     * Possible user input: "-en -ar -arna", "en ar arna", "-et - -en".
     * Therefore we remove all "-" and whitespaces.
     */
    private fun compareEndings(expected: String, userInput: String): Boolean {
        return expected.replace(
            regex = compareAlphabeticRegex,
            replacement = "",
        ).equals(userInput.replace(compareAlphabeticRegex, ""), ignoreCase = true)
    }

    private val compareAlphabeticRegex = Regex("[\\s-]")
}
