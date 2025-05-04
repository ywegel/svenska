package de.ywegel.svenska.domain.quiz.controller

import app.cash.turbine.test
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsController
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class TranslateWithEndingsControllerTest {

    @Test
    fun `initial state should have empty answer and endings`() = runTest {
        // Given
        val controller = TranslateWithEndingsController()

        // When & Then
        controller.state.test {
            val initialState = awaitItem()
            expectThat(initialState.translationInput).isEqualTo("")
            expectThat(initialState.endingInput).isEqualTo("")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    @DisplayName("Input Handling")
    inner class InputHandling {

        @Test
        fun `setting answer should update answer state only`() = runTest {
            // Given
            val controller = TranslateWithEndingsController()

            // When & Then
            controller.state.test {
                // Skip initial state
                awaitItem()

                // Act: change the answer
                controller.actions.onTranslationChanged("test")

                // Verify updated state
                val updatedState = awaitItem()
                expectThat(updatedState.translationInput).isEqualTo("test")
                expectThat(updatedState.endingInput).isEqualTo("") // endings shouldn't change

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `setting endings should update endings state only`() = runTest {
            // Given
            val controller = TranslateWithEndingsController()

            // When & Then
            controller.state.test {
                // Skip initial state
                awaitItem()

                // Act: change the endings
                controller.actions.onEndingChanged("-en")

                // Verify updated state
                val updatedState = awaitItem()
                expectThat(updatedState.translationInput).isEqualTo("") // answer shouldn't change
                expectThat(updatedState.endingInput).isEqualTo("-en")

                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Nested
    @DisplayName("User Answer Conversion")
    inner class UserAnswerConversion {

        @Test
        fun `toUserAnswer should return correct combined answer object`() = runTest {
            // Given
            val controller = TranslateWithEndingsController()
            controller.actions.onTranslationChanged("dog")
            controller.actions.onEndingChanged("-en")

            // When
            val userAnswer = controller.state.value.toUserAnswer()

            // Then
            expectThat(userAnswer).isEqualTo(UserAnswer.TranslateWithEndingsAnswer("dog", "-en"))
        }

        @Test
        fun `empty endings in state should convert to empty endings in user answer`() = runTest {
            // Given
            val controller = TranslateWithEndingsController()
            controller.actions.onTranslationChanged("dog")
            // Intentionally do not set endings

            // When
            val userAnswer = controller.state.value.toUserAnswer()

            // Then
            expectThat(userAnswer).isEqualTo(UserAnswer.TranslateWithEndingsAnswer("dog", ""))
        }
    }
}
