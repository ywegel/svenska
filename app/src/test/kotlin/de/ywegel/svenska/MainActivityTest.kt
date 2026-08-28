@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import de.ywegel.svenska.common.streamOf
import de.ywegel.svenska.data.preferences.keys.OnboardingPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

class MainActivityTest {

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
    fun `splash screen condition is true until preferences are loaded`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()
        val viewModel = MainViewModel(preferencesManager)

        // Simulate the splash screen condition in MainActivity
        val keepOnScreenCondition = { viewModel.mainUiState.value is MainUiState.Loading }

        // Then - splash screen should be kept on screen initially
        expectThat(keepOnScreenCondition()).isTrue()
        expectThat(startRouteFor(viewModel.mainUiState.value)).isNull()

        // When - simulate preferences loading
        advanceUntilIdle()

        // Then - splash screen should be dismissed after preferences are loaded
        expectThat(keepOnScreenCondition()).isFalse()
    }

    @Test
    fun `Integration test - Giving crash reporting consent hides consent sheet`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake {
            set(OnboardingPreferenceKeys.HasCompleted, true)
            set(PrivacyPreferenceKeys.AcknowledgedPolicyVersion, PrivacyPreferenceKeys.CURRENT_POLICY_VERSION)
        }
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle()
        expectThat(consentStepToShowFor(viewModel.mainUiState.value)).isEqualTo(ConsentStep.CrashReporting)

        // When
        preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, false)
        preferencesManager.update(PrivacyPreferenceKeys.ConsentDecisionTimestamp, "now")
        advanceUntilIdle()

        // Then
        expectThat(consentStepToShowFor(viewModel.mainUiState.value)).isNull()
    }

    @ParameterizedTest
    @MethodSource("provideConsentStepToShowForTestCases")
    fun `test all consentStepToShowFor cases`(state: MainUiState, expectedState: ConsentStep?) {
        expectThat(consentStepToShowFor(state)).isEqualTo(expectedState)
    }

    companion object {
        @JvmStatic
        fun provideConsentStepToShowForTestCases() = streamOf(
            // Hidden while loading
            Arguments.of(MainUiState.Loading, null),
            // Hidden before onboarding completed
            Arguments.of(
                MainUiState.Ready(
                    hasCompletedOnboarding = false,
                    consentStep = ConsentStep.Policy,
                ),
                null,
            ),
            // Shown when policy not acknowledged
            Arguments.of(
                MainUiState.Ready(
                    hasCompletedOnboarding = true,
                    consentStep = ConsentStep.Policy,
                ),
                ConsentStep.Policy,
            ),
            // Shown when crash reporting not acknowledged
            Arguments.of(
                MainUiState.Ready(
                    hasCompletedOnboarding = true,
                    consentStep = ConsentStep.CrashReporting,
                ),
                ConsentStep.CrashReporting,
            ),
            // Hidden when everything acknowledged
            Arguments.of(
                MainUiState.Ready(
                    hasCompletedOnboarding = true,
                    consentStep = ConsentStep.Done,
                ),
                null,
            ),
        )
    }
}
