package de.ywegel.svenska.domain.quiz.strategies

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.quiz.model.AdditionalInfo
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsResult
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class TranslationWithEndingsQuizStrategyTest {

    @Nested
    @DisplayName("GenerateQuestion")
    inner class GenerateQuestion {

        private val testVocabulary = Vocabulary(
            id = 1,
            word = "hund",
            translation = "dog",
            containerId = 1,
            ending = "-en -ar -arna",
            gender = Gender.Ultra,
            wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
        )

        @Test
        fun `when mode is Swedish, should create question with Swedish prompt and endings in expected answer`() {
            // Given
            val strategy = TranslationWithEndingsQuizStrategy(TranslateMode.SwedishToNative)

            // When
            val question = strategy.generateQuestion(testVocabulary)

            // Then
            expectThat(question).isEqualTo(
                QuizQuestion(
                    vocabularyId = 1,
                    prompt = "hund",
                    expectedAnswer = UserAnswer.TranslateWithEndingsAnswer("dog", null),
                    promptData = AdditionalInfo.PromptInfo(
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                        endings = "-en -ar -arna",
                        gender = Gender.Ultra,
                    ),
                ),
            )
        }

        @Test
        fun `when mode is Native, should create question with Native prompt`() {
            // Given
            val strategy = TranslationWithEndingsQuizStrategy(TranslateMode.NativeToSwedish)

            // When
            val question = strategy.generateQuestion(testVocabulary)

            // Then
            expectThat(question).isEqualTo(
                QuizQuestion(
                    vocabularyId = 1,
                    prompt = "dog",
                    expectedAnswer = UserAnswer.TranslateWithEndingsAnswer("hund", "-en -ar -arna"),
                    promptData = AdditionalInfo.SolutionInfo(
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                        endings = "-en -ar -arna",
                        gender = Gender.Ultra,
                    ),
                ),
            )
        }

        @Test
        fun `when mode is Random and random returns true, should create question with Swedish prompt`() {
            // Given
            val strategy = TranslationWithEndingsQuizStrategy(TranslateMode.Random) { true }

            // When
            val question = strategy.generateQuestion(testVocabulary)

            // Then
            expectThat(question).isEqualTo(
                QuizQuestion(
                    vocabularyId = 1,
                    prompt = "hund",
                    expectedAnswer = UserAnswer.TranslateWithEndingsAnswer("dog", null),
                    promptData = AdditionalInfo.PromptInfo(
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                        endings = "-en -ar -arna",
                        gender = Gender.Ultra,
                    ),
                ),
            )
        }

        @Test
        fun `when mode is Random and random returns false, should create question with Native prompt`() {
            // Given
            val strategy = TranslationWithEndingsQuizStrategy(TranslateMode.Random) { false }

            // When
            val question = strategy.generateQuestion(testVocabulary)

            // Then
            expectThat(question).isEqualTo(
                QuizQuestion(
                    vocabularyId = 1,
                    prompt = "dog",
                    expectedAnswer = UserAnswer.TranslateWithEndingsAnswer(
                        answer = "hund",
                        endings = "-en -ar -arna",
                    ),
                    promptData = AdditionalInfo.SolutionInfo(
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                        endings = "-en -ar -arna",
                        gender = Gender.Ultra,
                    ),
                ),
            )
        }
    }

    @Nested
    @DisplayName("ValidateAnswer")
    inner class ValidateAnswer {

        @Test
        fun `when both translation and endings are correct, should return correct result`() {
            // Given
            val strategy = TranslationWithEndingsQuizStrategy(TranslateMode.SwedishToNative)
            val question = QuizQuestion(
                vocabularyId = 1,
                prompt = "hund",
                expectedAnswer = UserAnswer.TranslateWithEndingsAnswer("dog", "-en -ar -arna"),
            )
            val userAnswer = UserAnswer.TranslateWithEndingsAnswer("dog", "-en -ar -arna")

            // When
            val result = strategy.validateAnswer(question, userAnswer)

            // Then
            expectThat(result).isEqualTo(
                TranslateWithEndingsResult(
                    translationCorrect = true,
                    endingsCorrect = true,
                ),
            )
        }

        @Test
        fun `when translation is correct but endings are wrong, should return partial correct result`() {
            // Given
            val strategy = TranslationWithEndingsQuizStrategy(TranslateMode.SwedishToNative)
            val question = QuizQuestion(
                vocabularyId = 1,
                prompt = "en bil",
                expectedAnswer = UserAnswer.TranslateWithEndingsAnswer("car", "-en -ar -arna"),
            )
            val userAnswer = UserAnswer.TranslateWithEndingsAnswer("car", "-et")

            // When
            val result = strategy.validateAnswer(question, userAnswer)

            // Then
            expectThat(result).isEqualTo(
                TranslateWithEndingsResult(
                    translationCorrect = true,
                    endingsCorrect = false,
                ),
            )
        }

        @Test
        fun `when translation is wrong but endings are correct, should return partial correct result`() {
            // Given
            val strategy = TranslationWithEndingsQuizStrategy(TranslateMode.SwedishToNative)
            val question = QuizQuestion(
                vocabularyId = 1,
                prompt = "en bil",
                expectedAnswer = UserAnswer.TranslateWithEndingsAnswer("car", "-en -ar -arna"),
            )
            val userAnswer = UserAnswer.TranslateWithEndingsAnswer("vehicle", "-en -ar -arna")

            // When
            val result = strategy.validateAnswer(question, userAnswer)

            // Then
            expectThat(result).isEqualTo(
                TranslateWithEndingsResult(
                    translationCorrect = false,
                    endingsCorrect = true,
                ),
            )
        }

        @Test
        fun `when both translation and endings are wrong, should return all incorrect result`() {
            // Given
            val strategy = TranslationWithEndingsQuizStrategy(TranslateMode.SwedishToNative)
            val question = QuizQuestion(
                vocabularyId = 1,
                prompt = "en bil",
                expectedAnswer = UserAnswer.TranslateWithEndingsAnswer("car", "-en -ar -arna"),
            )
            val userAnswer = UserAnswer.TranslateWithEndingsAnswer("vehicle", "-et")

            // When
            val result = strategy.validateAnswer(question, userAnswer)

            // Then
            expectThat(result).isEqualTo(
                TranslateWithEndingsResult(
                    translationCorrect = false,
                    endingsCorrect = false,
                ),
            )
        }
    }
}
