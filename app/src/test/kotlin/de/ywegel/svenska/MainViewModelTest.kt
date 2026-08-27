@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import app.cash.turbine.test
import de.ywegel.svenska.data.preferences.keys.LegalPreferenceKeys
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
    fun `mainUiState is initially Loading`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()

        // When
        val viewModel = MainViewModel(preferencesManager)

        // Then
        expectThat(viewModel.mainUiState.value).isEqualTo(MainUiState.Loading)
    }

    @Test
    fun `mainUiState is Ready with defaults when preferences are loaded`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()

        // When
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle()

        // Then
        viewModel.mainUiState.test {
            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(hasCompletedOnboarding = false, isLatestPrivacyPolicyAccepted = false),
            )
        }
    }

    @Test
    fun `mainUiState reflects completed onboarding`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake { set(OnboardingPreferenceKeys.HasCompleted, true) }

        // When
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle()

        // Then
        viewModel.mainUiState.test {
            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(hasCompletedOnboarding = true, isLatestPrivacyPolicyAccepted = false),
            )
        }
    }

    @Test
    fun `mainUiState reflects an already accepted privacy policy`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake {
            set(LegalPreferenceKeys.isLatestPrivacyPolicyAccepted, LegalPreferenceKeys.LATEST_PRIVACY_VERSION)
        }

        // When
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle()

        // Then
        viewModel.mainUiState.test {
            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(hasCompletedOnboarding = false, isLatestPrivacyPolicyAccepted = true),
            )
        }
    }

    @Test
    fun `mainUiState reflects changes to preferences`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow initial flow collection

        // When & Then
        viewModel.mainUiState.test {
            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(hasCompletedOnboarding = false, isLatestPrivacyPolicyAccepted = false),
            )

            // Update preferences
            preferencesManager.update(OnboardingPreferenceKeys.HasCompleted, true)
            advanceUntilIdle()

            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(hasCompletedOnboarding = true, isLatestPrivacyPolicyAccepted = false),
            )
        }
    }

    @Test
    fun `onPrivacyPolicyAccepted stores the latest privacy policy version`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake { set(OnboardingPreferenceKeys.HasCompleted, true) }
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle()

        // When & Then
        viewModel.mainUiState.test {
            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(hasCompletedOnboarding = true, isLatestPrivacyPolicyAccepted = false),
            )

            viewModel.onPrivacyPolicyAccepted()
            advanceUntilIdle()

            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(hasCompletedOnboarding = true, isLatestPrivacyPolicyAccepted = true),
            )
        }
    }
}
