package de.ywegel.svenska.domain.quiz.strategies

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.QuizQuestionPromptData
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class TranslationWithoutEndingsQuizStrategyTest {

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
        fun `when mode is Swedish, should create question with Swedish prompt`() {
            // Given
            val strategy = TranslationWithoutEndingsQuizStrategy(TranslateMode.Swedish)

            // When
            val question = strategy.generateQuestion(testVocabulary)

            // Then
            expectThat(question).isEqualTo(
                QuizQuestion(
                    vocabularyId = 1,
                    prompt = "hund",
                    expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("dog"),
                    promptData = QuizQuestionPromptData(
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
            val strategy = TranslationWithoutEndingsQuizStrategy(TranslateMode.Native)

            // When
            val question = strategy.generateQuestion(testVocabulary)

            // Then
            expectThat(question).isEqualTo(
                QuizQuestion(
                    vocabularyId = 1,
                    prompt = "dog",
                    expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("hund"),
                ),
            )
        }

        @Test
        fun `when mode is Random and random returns true, should create question with Swedish prompt`() {
            // Given
            val strategy = TranslationWithoutEndingsQuizStrategy(TranslateMode.Random) { true }

            // When
            val question = strategy.generateQuestion(testVocabulary)

            // Then
            expectThat(question).isEqualTo(
                QuizQuestion(
                    vocabularyId = 1,
                    prompt = "hund",
                    expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("dog"),
                    promptData = QuizQuestionPromptData(
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
            val strategy = TranslationWithoutEndingsQuizStrategy(TranslateMode.Random) { false }

            // When
            val question = strategy.generateQuestion(testVocabulary)

            // Then
            expectThat(question).isEqualTo(
                QuizQuestion(
                    vocabularyId = 1,
                    prompt = "dog",
                    expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("hund"),
                ),
            )
        }
    }

    @Nested
    @DisplayName("ValidateAnswer")
    inner class ValidateAnswer {

        @Test
        fun `when answer matches expected exactly, should return true`() {
            // Given
            val strategy = TranslationWithoutEndingsQuizStrategy(TranslateMode.Swedish)
            val question = QuizQuestion(
                vocabularyId = 1,
                prompt = "hund",
                expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("dog"),
            )
            val userAnswer = UserAnswer.TranslateWithoutEndingsAnswer("dog")

            // When
            val result = strategy.validateAnswer(question, userAnswer)

            // Then
            expectThat(result).isTrue()
        }

        @Test
        fun `when answer matches expected with different case, should return true`() {
            // Given
            val strategy = TranslationWithoutEndingsQuizStrategy(TranslateMode.Swedish)
            val question = QuizQuestion(
                vocabularyId = 1,
                prompt = "hund",
                expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("Dog"),
            )
            val userAnswer = UserAnswer.TranslateWithoutEndingsAnswer("dog")

            // When
            val result = strategy.validateAnswer(question, userAnswer)

            // Then
            expectThat(result).isTrue()
        }

        @Test
        fun `when answer has extra whitespace, should return true`() {
            // Given
            val strategy = TranslationWithoutEndingsQuizStrategy(TranslateMode.Swedish)
            val question = QuizQuestion(
                vocabularyId = 1,
                prompt = "hund",
                expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("dog"),
            )
            val userAnswer = UserAnswer.TranslateWithoutEndingsAnswer(" dog ")

            // When
            val result = strategy.validateAnswer(question, userAnswer)

            // Then
            expectThat(result).isTrue()
        }

        @Test
        fun `when answer is different, should return false`() {
            // Given
            val strategy = TranslationWithoutEndingsQuizStrategy(TranslateMode.Swedish)
            val question = QuizQuestion(
                vocabularyId = 1,
                prompt = "hund",
                expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("dog"),
            )
            val userAnswer = UserAnswer.TranslateWithoutEndingsAnswer("cat")

            // When
            val result = strategy.validateAnswer(question, userAnswer)

            // Then
            expectThat(result).isFalse()
        }
    }
}
