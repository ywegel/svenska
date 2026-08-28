@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import app.cash.turbine.test
import de.ywegel.svenska.data.preferences.keys.OnboardingPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
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
        // When
        val viewModel = MainViewModel(UserPreferencesManagerFake())

        // Then
        expectThat(viewModel.mainUiState.value).isEqualTo(MainUiState.Loading)
    }

    @Test
    fun `full walkthrough of onboarding and opt-in logic`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle()

        // When & Then
        viewModel.mainUiState.test {
            // Initial state, after opening the app the first time
            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(hasCompletedOnboarding = false, consentStep = ConsentStep.Policy),
            )

            // Finish onboarding
            preferencesManager.update(OnboardingPreferenceKeys.HasCompleted, true)
            advanceUntilIdle()
            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(
                    hasCompletedOnboarding = true,
                    consentStep = ConsentStep.Policy,
                ),
            )

            // Accept Policy
            preferencesManager.update(
                PrivacyPreferenceKeys.AcknowledgedPolicyVersion,
                PrivacyPreferenceKeys.CURRENT_POLICY_VERSION,
            )
            advanceUntilIdle()
            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(
                    hasCompletedOnboarding = true,
                    consentStep = ConsentStep.CrashReporting,
                ),
            )

            // Opt into crash reporting
            preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, true)
            preferencesManager.update(PrivacyPreferenceKeys.ConsentDecisionTimestamp, "now")
            advanceUntilIdle()
            expectThat(awaitItem()).isEqualTo(
                MainUiState.Ready(
                    hasCompletedOnboarding = true,
                    consentStep = ConsentStep.Done,
                ),
            )
        }
    }
}
