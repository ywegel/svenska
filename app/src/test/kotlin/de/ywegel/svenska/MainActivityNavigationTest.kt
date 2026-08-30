@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import com.ramcosta.composedestinations.generated.destinations.ContainerScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OnboardingScreenDestination
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
import strikt.assertions.isNull

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
    fun `app navigates nowhere while loading`() {
        expectThat(startRouteFor(MainUiState.Loading)).isNull()
    }

    @Test
    fun `app navigates to OnboardingScreen when onboarding is not completed`() {
        val destination =
            startRouteFor(MainUiState.Ready(hasCompletedOnboarding = false, consentStep = ConsentStep.Policy))
        expectThat(destination)
            .isEqualTo(OnboardingScreenDestination)
    }

    @Test
    fun `app navigates to ContainerScreen when onboarding is completed`() {
        // We navigate, even if consents are not given. They are shown in the modal overlapping the ContainerScreen
        val destination =
            startRouteFor(MainUiState.Ready(hasCompletedOnboarding = true, consentStep = ConsentStep.Policy))
        expectThat(destination)
            .isEqualTo(ContainerScreenDestination)
    }

    @Test
    fun `app navigates to ContainerScreen after completing onboarding`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()
        val mainViewModel = MainViewModel(preferencesManager)
        val onboardingViewModel = OnboardingViewModel(preferencesManager, testDispatcher)
        advanceUntilIdle()

        // When
        onboardingViewModel.completeOnboarding()
        advanceUntilIdle()

        // Then
        expectThat(mainViewModel.mainUiState.value).isEqualTo(
            MainUiState.Ready(hasCompletedOnboarding = true, consentStep = ConsentStep.Policy),
        )
        expectThat(startRouteFor(mainViewModel.mainUiState.value))
            .isEqualTo(ContainerScreenDestination)
    }
}
