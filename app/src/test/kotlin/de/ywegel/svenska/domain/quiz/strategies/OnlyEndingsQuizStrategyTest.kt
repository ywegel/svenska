package de.ywegel.svenska.domain.quiz.strategies

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.quiz.model.AdditionalInfo
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class OnlyEndingsQuizStrategyTest {

    private fun validateAnswer(expectedEnding: String, userEnding: String): Boolean {
        val question = QuizQuestion(
            vocabularyId = 1,
            prompt = "bok",
            expectedAnswer = UserAnswer.OnlyEndingsAnswer(expectedEnding),
            promptData = AdditionalInfo.SolutionInfo(null, expectedEnding, null),
        )
        val userAnswer = UserAnswer.OnlyEndingsAnswer(userEnding)
        return OnlyEndingsQuizStrategy().validateAnswer(question, userAnswer)
    }

    @Nested
    inner class GenerateQuestion {
        @Test
        fun `given vocabulary, when generating question, then prompt is word and answer is ending`() {
            // Given
            val strategy = OnlyEndingsQuizStrategy()

            val testVocabulary = Vocabulary(
                id = 1,
                word = "bok",
                translation = "Buch",
                gender = Gender.Neutra,
                wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.N),
                ending = "-en",
                notes = "",
                irregularPronunciation = null,
                isFavorite = false,
                containerId = 1,
                lastEdited = System.currentTimeMillis(),
                created = System.currentTimeMillis(),
            )

            // When
            val question = strategy.generateQuestion(testVocabulary)

            // Then
            expectThat(question) {
                get { vocabularyId }.isEqualTo(testVocabulary.id)
                get { prompt }.isEqualTo(testVocabulary.word)
                get { expectedAnswer }.isEqualTo(UserAnswer.OnlyEndingsAnswer(testVocabulary.ending))
                get { promptData as AdditionalInfo.SolutionInfo }.and {
                    get { wordGroup }.isEqualTo(testVocabulary.wordGroup)
                    get { endings }.isEqualTo(testVocabulary.ending)
                    get { gender }.isEqualTo(testVocabulary.gender)
                }
            }
        }
    }

    @Nested
    inner class ValidateAnswer {
        @Test
        fun `given correct ending, when validating, then returns true`() {
            // When
            val result = validateAnswer(expectedEnding = "-en", userEnding = "-en")

            // Then
            expectThat(result).isTrue()
        }

        @Test
        fun `given correct ending with extra spaces, when validating, then returns true`() {
            // When
            val result = validateAnswer(expectedEnding = "-en", userEnding = "  -en  ")

            // Then
            expectThat(result).isTrue()
        }

        @Test
        fun `given correct ending without dash, when validating, then returns true`() {
            // When
            val result = validateAnswer(expectedEnding = "-en", userEnding = "en")

            // Then
            expectThat(result).isTrue()
        }

        @Test
        fun `given wrong ending, when validating, then returns false`() {
            // When
            val result = validateAnswer(expectedEnding = "-en", userEnding = "-er")

            // Then
            expectThat(result).isFalse()
        }

        @Test
        fun `given empty user answer, when validating, then returns false`() {
            // When
            val result = validateAnswer(expectedEnding = "-en", userEnding = "")

            // Then
            expectThat(result).isFalse()
        }

        @Test
        fun `given empty expected answer, when validating with empty user answer, then returns true`() {
            // When
            val result = validateAnswer(expectedEnding = "", userEnding = "")

            // Then
            expectThat(result).isTrue()
        }
    }
}
