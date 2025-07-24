package de.ywegel.svenska.domain.quiz.controller

import app.cash.turbine.test
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.quiz.controller.OnlyEndingsController
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class OnlyEndingsControllerTest {

    @Test
    fun `initial state should have empty endings input`() = runTest {
        // Given
        val controller = OnlyEndingsController()

        // When & Then
        controller.state.test {
            val initialState = awaitItem()
            expectThat(initialState.endingsInput).isEqualTo("")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Nested
    @DisplayName("Endings Handling")
    inner class EndingsHandling {

        @Test
        fun `setting endings should update state`() = runTest {
            // Given
            val controller = OnlyEndingsController()

            // When & Then
            controller.state.test {
                // Verify initial state
                expectThat(awaitItem().endingsInput).isEqualTo("")

                // Act: change the endings
                controller.actions.onEndingsChanged("-en")

                // Verify updated state
                expectThat(awaitItem().endingsInput).isEqualTo("-en")

                cancelAndIgnoreRemainingEvents()
            }
        }

        @Test
        fun `toUserAnswer should return correct answer object`() = runTest {
            // Given
            val controller = OnlyEndingsController()
            controller.actions.onEndingsChanged("-en")

            // When
            val userAnswer = controller.state.value.toUserAnswer()

            // Then
            expectThat(userAnswer).isEqualTo(UserAnswer.OnlyEndingsAnswer("-en"))
        }
    }
}
