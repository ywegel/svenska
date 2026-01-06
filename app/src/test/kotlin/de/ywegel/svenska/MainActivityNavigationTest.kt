@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OnboardingScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import de.ywegel.svenska.de.ywegel.svenska.domain.main.LoadInitialAppStateUseCaseFake
import de.ywegel.svenska.domain.main.AcceptLatestPrivacyPolicyUseCase
import de.ywegel.svenska.domain.main.InitialAppState
import de.ywegel.svenska.domain.main.LoadInitialAppStateUseCaseImpl
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

    private val acceptLatestPrivacyPolicyFake = object : AcceptLatestPrivacyPolicyUseCase {
        override suspend fun invoke() {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun `app navigates to OnboardingScreen when onboarding is not completed`() = runTest(testDispatcher) {
        // Given
        val loadAppStateFake = LoadInitialAppStateUseCaseFake(
            initialAppState = InitialAppState(
                hasCompletedOnboarding = false,
                isLatestPrivacyPolicyAccepted = false,
            ),
        )
        val viewModel = MainViewModel(loadAppStateFake, acceptLatestPrivacyPolicyFake)
        advanceUntilIdle() // Allow preferences to load

        // When
        val startRoute = determineStartRoute(viewModel.mainUiState.value)

        // Then
        expectThat(startRoute).isEqualTo(OnboardingScreenDestination)
    }

    @Test
    fun `app navigates to ContainerScreen when onboarding is completed`() = runTest(testDispatcher) {
        // Given
        val loadAppStateFake = LoadInitialAppStateUseCaseFake(
            initialAppState = InitialAppState(
                hasCompletedOnboarding = true,
                isLatestPrivacyPolicyAccepted = false,
            ),
        )
        val viewModel = MainViewModel(loadAppStateFake, acceptLatestPrivacyPolicyFake)
        advanceUntilIdle() // Allow preferences to load

        // When
        val startRoute = determineStartRoute(viewModel.mainUiState.value)

        // Then
        expectThat(startRoute).isEqualTo(ContainerScreenDestination)
    }

    @Test
    fun `app navigates to ContainerScreen after completing onboarding`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake(initialHasCompletedOnboarding = false)
        val mainViewModel =
            MainViewModel(LoadInitialAppStateUseCaseImpl(preferencesManager), acceptLatestPrivacyPolicyFake)
        val onboardingViewModel = OnboardingViewModel(preferencesManager, testDispatcher)
        advanceUntilIdle() // Allow preferences to load

        // When - simulate completing onboarding
        onboardingViewModel.completeOnboarding()
        advanceUntilIdle() // Allow preferences to update

        // Then
        val state = mainViewModel.mainUiState.value as MainUiState.Ready
        expectThat(state.hasCompletedOnboarding).isEqualTo(true)
        expectThat(determineStartRoute(mainViewModel.mainUiState.value))
            .isEqualTo(ContainerScreenDestination)
    }

    // Helper function to simulate the logic in MainActivity
    private fun determineStartRoute(state: MainUiState): Direction {
        val onboardingCompleted = (state as MainUiState.Ready).hasCompletedOnboarding
        return if (!onboardingCompleted) {
            OnboardingScreenDestination
        } else {
            ContainerScreenDestination
        }
    }
}
