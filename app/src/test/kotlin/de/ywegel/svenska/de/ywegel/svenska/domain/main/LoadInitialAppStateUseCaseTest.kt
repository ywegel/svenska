@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.de.ywegel.svenska.domain.main

import app.cash.turbine.test
import de.ywegel.svenska.data.preferences.LATEST_PRIVACY_VERSION
import de.ywegel.svenska.domain.main.InitialAppState
import de.ywegel.svenska.domain.main.LoadInitialAppStateUseCaseImpl
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import io.mockk.clearAllMocks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class LoadInitialAppStateUseCaseTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun cleanUp() {
        clearAllMocks()
        Dispatchers.resetMain()
    }

    @Test
    fun `hasCompletedOnboarding is correctly reflected in AppState`() = runTest(testDispatcher) {
        val fakePreferencesManager =
            UserPreferencesManagerFake(initialHasCompletedOnboarding = false, initialAcceptedPrivacyVersion = -1)
        val useCase = LoadInitialAppStateUseCaseImpl(fakePreferencesManager)

        useCase().test {
            expectThat(awaitItem()).isEqualTo(
                InitialAppState(
                    hasCompletedOnboarding = false,
                    isLatestPrivacyPolicyAccepted = false,
                ),
            )

            fakePreferencesManager.updateHasCompletedOnboarding(true)

            expectThat(awaitItem()).isEqualTo(
                InitialAppState(
                    hasCompletedOnboarding = true,
                    isLatestPrivacyPolicyAccepted = false,
                ),
            )
        }
    }

    @Test
    fun `isLatestPrivacyPolicyAccepted is correctly determined`() = runTest(testDispatcher) {
        val fakePreferencesManager =
            UserPreferencesManagerFake(initialHasCompletedOnboarding = false, initialAcceptedPrivacyVersion = -1)
        val useCase = LoadInitialAppStateUseCaseImpl(fakePreferencesManager)

        useCase().test {
            expectThat(awaitItem()).isEqualTo(
                InitialAppState(
                    hasCompletedOnboarding = false,
                    isLatestPrivacyPolicyAccepted = false,
                ),
            )

            fakePreferencesManager.acceptLatestPrivacyPolicy()

            expectThat(awaitItem()).isEqualTo(
                InitialAppState(
                    hasCompletedOnboarding = false,
                    isLatestPrivacyPolicyAccepted = true,
                ),
            )
        }
    }

    @Test
    fun `Privacy policy is accepted, if initial preferences value is accepted`() = runTest(testDispatcher) {
        val fakePreferencesManager = UserPreferencesManagerFake(
            initialHasCompletedOnboarding = false,
            initialAcceptedPrivacyVersion = LATEST_PRIVACY_VERSION,
        )
        val useCase = LoadInitialAppStateUseCaseImpl(fakePreferencesManager)

        useCase().test {
            expectThat(awaitItem()).isEqualTo(
                InitialAppState(
                    hasCompletedOnboarding = false,
                    isLatestPrivacyPolicyAccepted = true,
                ),
            )
        }
    }
}
