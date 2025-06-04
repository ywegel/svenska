@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import app.cash.turbine.test
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNull

class MainViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun cleanUp() {
        Dispatchers.resetMain()
    }

    @Test
    fun `hasCompletedOnboarding is initially null`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake(initialHasCompletedOnboarding = false)

        // When
        val viewModel = MainViewModel(preferencesManager)

        // Then
        expectThat(viewModel.hasCompletedOnboarding.value).isNull()
    }

    @Test
    fun `hasCompletedOnboarding is updated when preferences are loaded`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake(initialHasCompletedOnboarding = true)

        // When
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow the flow collection to complete

        // Then
        viewModel.hasCompletedOnboarding.test {
            expectThat(awaitItem()).isEqualTo(true)
        }
    }

    @Test
    fun `hasCompletedOnboarding reflects changes to preferences`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake(initialHasCompletedOnboarding = false)
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow initial flow collection

        // When & Then
        viewModel.hasCompletedOnboarding.test {
            expectThat(awaitItem()).isEqualTo(false)

            // Update preferences
            preferencesManager.updateHasCompletedOnboarding(true)
            advanceUntilIdle()

            expectThat(awaitItem()).isEqualTo(true)
        }
    }
}
