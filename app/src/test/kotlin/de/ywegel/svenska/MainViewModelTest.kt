@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska

import app.cash.turbine.test
import de.ywegel.svenska.de.ywegel.svenska.domain.main.LoadInitialAppStateUseCaseFake
import de.ywegel.svenska.domain.main.AcceptLatestPrivacyPolicyUseCase
import de.ywegel.svenska.domain.main.InitialAppState
import de.ywegel.svenska.domain.main.LoadInitialAppStateUseCaseImpl
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import io.mockk.coVerify
import io.mockk.mockk
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

    private val acceptLatestPrivacyPolicyFake = object : AcceptLatestPrivacyPolicyUseCase {
        override suspend fun invoke() {
            TODO("Not yet implemented")
        }
    }

    @Test
    fun `MainUiState is initially Loading`() = runTest(testDispatcher) {
        // Given
        val loadAppStateFake = LoadInitialAppStateUseCaseFake(
            initialAppState = InitialAppState(
                hasCompletedOnboarding = true,
                isLatestPrivacyPolicyAccepted = false,
            ),
        )

        // When
        val viewModel = MainViewModel(loadAppStateFake, acceptLatestPrivacyPolicyFake)

        // Then
        expectThat(viewModel.mainUiState.value).isEqualTo(MainUiState.Loading)
    }

    @Test
    fun `MainUiState is updated when preferences are loaded`() = runTest(testDispatcher) {
        // Given
        val loadAppStateFake = LoadInitialAppStateUseCaseFake(
            initialAppState = InitialAppState(
                hasCompletedOnboarding = true,
                isLatestPrivacyPolicyAccepted = false,
            ),
        )

        // When
        val viewModel = MainViewModel(loadAppStateFake, acceptLatestPrivacyPolicyFake)
        advanceUntilIdle() // Allow the flow collection to complete

        // Then
        viewModel.mainUiState.test {
            val emittedState = awaitItem().testAsReady
            expectThat(emittedState.hasCompletedOnboarding).isEqualTo(true)
            expectThat(emittedState.isLatestPrivacyPolicyAccepted).isEqualTo(false)
        }
    }

    @Test
    fun `hasCompletedOnboarding reflects changes to preferences`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake(initialHasCompletedOnboarding = false)
        val viewModel = MainViewModel(
            loadInitialAppState = LoadInitialAppStateUseCaseImpl(preferencesManager),
            acceptLatestPrivacyPolicy = acceptLatestPrivacyPolicyFake,
        )
        advanceUntilIdle() // Allow initial flow collection

        // When & Then
        viewModel.mainUiState.test {
            expectThat(awaitItem().testAsReady.hasCompletedOnboarding).isEqualTo(false)

            // Update preferences
            preferencesManager.updateHasCompletedOnboarding(true)
            advanceUntilIdle()

            expectThat(awaitItem().testAsReady.hasCompletedOnboarding).isEqualTo(true)
        }
    }

    @Test
    fun `isLatestPrivacyPolicyAccepted reflects changes to preferences`() = runTest(testDispatcher) {
        // Given
        val loadAppStateFake = LoadInitialAppStateUseCaseFake(
            initialAppState = InitialAppState(
                hasCompletedOnboarding = true,
                isLatestPrivacyPolicyAccepted = false,
            ),
        )
        val viewModel = MainViewModel(loadAppStateFake, acceptLatestPrivacyPolicyFake)
        advanceUntilIdle()
        viewModel.mainUiState.test {
            expectThat(awaitItem().testAsReady.isLatestPrivacyPolicyAccepted).isFalse()

            // When
            loadAppStateFake.emitNewState(
                InitialAppState(
                    hasCompletedOnboarding = true,
                    isLatestPrivacyPolicyAccepted = true,
                ),
            )

            // Then
            expectThat(awaitItem().testAsReady.isLatestPrivacyPolicyAccepted).isTrue()
        }
    }

    @Test
    fun `onPrivacyPolicyAccepted calls AcceptLatestPrivacyPolicyUseCase`() = runTest(testDispatcher) {
        // Given
        val acceptUseCase = mockk<AcceptLatestPrivacyPolicyUseCase>(relaxed = true)
        val viewModel = MainViewModel(mockk(relaxed = true), acceptUseCase)

        // When
        viewModel.onPrivacyPolicyAccepted()
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { acceptUseCase() }
    }
}

private val MainUiState.testAsReady: MainUiState.Ready
    get() = this as MainUiState.Ready
