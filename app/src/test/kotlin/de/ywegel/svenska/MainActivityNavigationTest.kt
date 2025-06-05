@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OnboardingScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import de.ywegel.svenska.ui.onboarding.OnboardingViewModel
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

class MainActivityNavigationTest {

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
    fun `app navigates to OnboardingScreen when onboarding is not completed`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake(initialHasCompletedOnboarding = false)
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow preferences to load

        // When
        val startRoute = determineStartRoute(viewModel.hasCompletedOnboarding.value)

        // Then
        expectThat(startRoute).isEqualTo(OnboardingScreenDestination)
    }

    @Test
    fun `app navigates to ContainerScreen when onboarding is completed`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake(initialHasCompletedOnboarding = true)
        val viewModel = MainViewModel(preferencesManager)
        advanceUntilIdle() // Allow preferences to load

        // When
        val startRoute = determineStartRoute(viewModel.hasCompletedOnboarding.value)

        // Then
        expectThat(startRoute).isEqualTo(ContainerScreenDestination)
    }

    @Test
    fun `app navigates to ContainerScreen after completing onboarding`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake(initialHasCompletedOnboarding = false)
        val mainViewModel = MainViewModel(preferencesManager)
        val onboardingViewModel = OnboardingViewModel(preferencesManager, testDispatcher)
        advanceUntilIdle() // Allow preferences to load

        // When - simulate completing onboarding
        onboardingViewModel.completeOnboarding()
        advanceUntilIdle() // Allow preferences to update

        // Then
        expectThat(mainViewModel.hasCompletedOnboarding.value).isEqualTo(true)
        expectThat(determineStartRoute(mainViewModel.hasCompletedOnboarding.value))
            .isEqualTo(ContainerScreenDestination)
    }

    // Helper function to simulate the logic in MainActivity
    private fun determineStartRoute(onboardingCompleted: Boolean?): Direction {
        return if (onboardingCompleted != true) {
            OnboardingScreenDestination
        } else {
            ContainerScreenDestination
        }
    }
}
