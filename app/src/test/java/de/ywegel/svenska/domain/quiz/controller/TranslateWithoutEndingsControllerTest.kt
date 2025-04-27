package de.ywegel.svenska.domain.quiz.controller

import app.cash.turbine.test
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.quiz.controller.TranslateWithoutEndingsController
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class TranslateWithoutEndingsControllerTest {

    @Test
    fun `initial state should have empty answer`() = runTest {
        // Given
        val controller = TranslateWithoutEndingsController()

        // When & Then
        controller.state.test {
            val initialState = awaitItem()
            expectThat(initialState.translationInput).isEqualTo("")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    @DisplayName("Answer Handling")
    inner class AnswerHandling {

        @Test
        fun `setting answer should update state`() = runTest {
            // Given
            val controller = TranslateWithoutEndingsController()

            // When & Then
            controller.state.test {
                // Verify initial state
                expectThat(awaitItem().translationInput).isEqualTo("")

                // Act: change the answer
                controller.actions.onInputChanged("test")

                // Verify updated state
                expectThat(awaitItem().translationInput).isEqualTo("test")

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `toUserAnswer should return correct answer object`() = runTest {
            // Given
            val controller = TranslateWithoutEndingsController()
            controller.actions.onInputChanged("dog")

            // When
            val userAnswer = controller.state.value.toUserAnswer()

            // Then
            expectThat(userAnswer).isEqualTo(UserAnswer.TranslateWithoutEndingsAnswer("dog"))
        }
    }
}
