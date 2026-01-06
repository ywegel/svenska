@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OnboardingScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import de.ywegel.svenska.de.ywegel.svenska.domain.main.LoadInitialAppStateUseCaseFake
import de.ywegel.svenska.domain.main.AcceptLatestPrivacyPolicyUseCase
import de.ywegel.svenska.domain.main.InitialAppState
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

    private val acceptLatestPrivacyPolicyFake = object : AcceptLatestPrivacyPolicyUseCase {
        override suspend fun invoke() {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun `splash screen condition is true until preferences are loaded`() = runTest(testDispatcher) {
        // Given
        val loadAppStateFake = LoadInitialAppStateUseCaseFake(
            initialAppState = InitialAppState(
                hasCompletedOnboarding = false,
                isLatestPrivacyPolicyAccepted = false,
            ),
        )
        val viewModel = MainViewModel(loadAppStateFake, acceptLatestPrivacyPolicyFake)

        // Simulate the splash screen condition in MainActivity
        val keepOnScreenCondition = { viewModel.mainUiState.value is MainUiState.Loading }

        // Then - splash screen should be kept on screen initially
        expectThat(keepOnScreenCondition()).isTrue()

        // When - simulate preferences loading
        advanceUntilIdle()

        // Then - splash screen should be dismissed after preferences are loaded
        expectThat(keepOnScreenCondition()).isFalse()
    }

    @Test
    fun `start route is set to OnboardingScreen when onboarding is not completed`() = runTest(testDispatcher) {
        // Given
        val loadAppStateFake = LoadInitialAppStateUseCaseFake(
            initialAppState = InitialAppState(
                hasCompletedOnboarding = false,
                isLatestPrivacyPolicyAccepted = false,
            ),
        )
        val viewModel = MainViewModel(loadAppStateFake, acceptLatestPrivacyPolicyFake)
        advanceUntilIdle() // Allow preferences to load

        // When & Then
        expectThat(viewModel.mainUiState.value.testAsReady.hasCompletedOnboarding).isEqualTo(false)
        expectThat(determineStartRoute(viewModel.mainUiState.value))
            .isEqualTo(OnboardingScreenDestination)
    }

    @Test
    fun `start route is set to ContainerScreen when onboarding is completed`() = runTest(testDispatcher) {
        // Given
        val loadAppStateFake = LoadInitialAppStateUseCaseFake(
            initialAppState = InitialAppState(
                hasCompletedOnboarding = true,
                isLatestPrivacyPolicyAccepted = false,
            ),
        )
        val viewModel = MainViewModel(loadAppStateFake, acceptLatestPrivacyPolicyFake)
        advanceUntilIdle() // Allow preferences to load

        // When & Then
        expectThat(viewModel.mainUiState.value.testAsReady.hasCompletedOnboarding).isEqualTo(true)
        expectThat(determineStartRoute(viewModel.mainUiState.value)).isEqualTo(ContainerScreenDestination)
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

private val MainUiState.testAsReady: MainUiState.Ready
    get() = this as MainUiState.Ready
