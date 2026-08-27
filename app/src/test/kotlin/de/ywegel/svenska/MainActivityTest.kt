@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OnboardingScreenDestination
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
    fun `start route is set to OnboardingScreen when onboarding is not completed`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow preferences to load

        // When & Then
        expectThat(viewModel.mainUiState.value).isEqualTo(
            MainUiState.Ready(hasCompletedOnboarding = false, isLatestPrivacyPolicyAccepted = false),
        )
        expectThat(startRouteFor(viewModel.mainUiState.value)).isEqualTo(OnboardingScreenDestination)
    }

    @Test
    fun `start route is set to ContainerScreen when onboarding is completed`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake { set(OnboardingPreferenceKeys.HasCompleted, true) }
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow preferences to load

        // When & Then
        expectThat(viewModel.mainUiState.value).isEqualTo(
            MainUiState.Ready(hasCompletedOnboarding = true, isLatestPrivacyPolicyAccepted = false),
        )
        expectThat(startRouteFor(viewModel.mainUiState.value)).isEqualTo(ContainerScreenDestination)
    }

    @Test
    fun `start route is set to ContainerScreen regardless of privacy policy acceptance`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake { set(OnboardingPreferenceKeys.HasCompleted, true) }
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle()

        // When & Then
        expectThat(startRouteFor(viewModel.mainUiState.value)).isEqualTo(ContainerScreenDestination)
    }

    @Test
    fun `privacy policy bottom sheet is hidden while loading`() {
        expectThat(shouldShowPrivacyPolicyBottomSheet(MainUiState.Loading)).isFalse()
    }

    @Test
    fun `privacy policy bottom sheet is hidden before onboarding is completed`() {
        val state = MainUiState.Ready(hasCompletedOnboarding = false, isLatestPrivacyPolicyAccepted = false)

        expectThat(shouldShowPrivacyPolicyBottomSheet(state)).isFalse()
    }

    @Test
    fun `privacy policy bottom sheet is shown after onboarding when policy is not accepted`() {
        val state = MainUiState.Ready(hasCompletedOnboarding = true, isLatestPrivacyPolicyAccepted = false)

        expectThat(shouldShowPrivacyPolicyBottomSheet(state)).isTrue()
    }

    @Test
    fun `privacy policy bottom sheet is hidden once the policy is accepted`() {
        val state = MainUiState.Ready(hasCompletedOnboarding = true, isLatestPrivacyPolicyAccepted = true)

        expectThat(shouldShowPrivacyPolicyBottomSheet(state)).isFalse()
    }

    @Test
    fun `accepting the privacy policy hides the bottom sheet`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake { set(OnboardingPreferenceKeys.HasCompleted, true) }
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle()
        expectThat(shouldShowPrivacyPolicyBottomSheet(viewModel.mainUiState.value)).isTrue()

        // When
        viewModel.onPrivacyPolicyAccepted()
        advanceUntilIdle()

        // Then
        expectThat(shouldShowPrivacyPolicyBottomSheet(viewModel.mainUiState.value)).isFalse()
    }
}
