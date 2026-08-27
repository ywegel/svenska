@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import app.cash.turbine.test
import de.ywegel.svenska.data.preferences.keys.OnboardingPreferenceKeys
import de.ywegel.svenska.data.preferences.set
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
    fun `onboardingState is initially Loading`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()

        // When
        val viewModel = MainViewModel(preferencesManager)

        // Then
        expectThat(viewModel.onboardingState.value).isEqualTo(OnboardingState.Loading)
    }

    @Test
    fun `onboardingState is updated when preferences are loaded`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake { set(OnboardingPreferenceKeys.HasCompleted, true) }

        // When
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow the flow collection to complete

        // Then
        viewModel.onboardingState.test {
            expectThat(awaitItem()).isEqualTo(OnboardingState.Completed)
        }
    }

    @Test
    fun `onboardingState reflects changes to preferences`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow initial flow collection

        // When & Then
        viewModel.onboardingState.test {
            expectThat(awaitItem()).isEqualTo(OnboardingState.NotCompleted)

            // Update preferences
            preferencesManager.update(OnboardingPreferenceKeys.HasCompleted, true)
            advanceUntilIdle()

            expectThat(awaitItem()).isEqualTo(OnboardingState.Completed)
        }
    }
}
