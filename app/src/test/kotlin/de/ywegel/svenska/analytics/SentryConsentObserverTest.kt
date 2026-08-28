@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.analytics

import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import de.ywegel.svenska.data.preferences.set
import de.ywegel.svenska.fakes.SentryControllerFake
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue

class SentryConsentObserverTest {

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
    fun `sentry is not initialized while crash reporting is not enabled`() = runTest(testDispatcher) {
        // Given
        val sentryController = SentryControllerFake()
        val observer = SentryConsentObserver(UserPreferencesManagerFake(), sentryController)

        // When
        observer.start(TestScope(testDispatcher))
        advanceUntilIdle()

        // Then
        expectThat(sentryController.isInitialized).isFalse()
    }

    @Test
    fun `sentry initializes immediately once crash reporting is enabled`() = runTest(testDispatcher) {
        // Given
        val sentryController = SentryControllerFake()
        val preferencesManager = UserPreferencesManagerFake()
        val observer = SentryConsentObserver(preferencesManager, sentryController)
        observer.start(TestScope(testDispatcher))
        advanceUntilIdle()
        expectThat(sentryController.isInitialized).isFalse()

        // When
        preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, true)
        advanceUntilIdle()

        // Then
        expectThat(sentryController.isInitialized).isTrue()
    }

    @Test
    fun `sentry shuts down immediately once crash reporting is disabled again`() = runTest(testDispatcher) {
        // Given
        val sentryController = SentryControllerFake()
        val preferencesManager = UserPreferencesManagerFake {
            set(PrivacyPreferenceKeys.CrashReportingEnabled, true)
        }
        val observer = SentryConsentObserver(preferencesManager, sentryController)
        observer.start(TestScope(testDispatcher))
        advanceUntilIdle()
        expectThat(sentryController.isInitialized).isTrue()

        // When
        preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, false)
        advanceUntilIdle()

        // Then
        expectThat(sentryController.isInitialized).isFalse()
    }
}
