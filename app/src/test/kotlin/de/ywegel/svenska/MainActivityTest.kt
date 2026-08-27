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
        val keepOnScreenCondition = { viewModel.onboardingState.value is OnboardingState.Loading }

        // Then - splash screen should be kept on screen initially
        expectThat(keepOnScreenCondition()).isTrue()
        expectThat(startRouteFor(viewModel.onboardingState.value)).isNull()

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
        expectThat(viewModel.onboardingState.value).isEqualTo(OnboardingState.NotCompleted)
        expectThat(startRouteFor(viewModel.onboardingState.value)).isEqualTo(OnboardingScreenDestination)
    }

    @Test
    fun `start route is set to ContainerScreen when onboarding is completed`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake { set(OnboardingPreferenceKeys.HasCompleted, true) }
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow preferences to load

        // When & Then
        expectThat(viewModel.onboardingState.value).isEqualTo(OnboardingState.Completed)
        expectThat(startRouteFor(viewModel.onboardingState.value)).isEqualTo(ContainerScreenDestination)
    }
}
