@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.analytics

import de.ywegel.svenska.data.preferences.PreferenceKey
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import de.ywegel.svenska.data.preferences.set
import de.ywegel.svenska.domain.GetOrCreateCrashReportingIdUseCase
import de.ywegel.svenska.fakes.SentryControllerFake
import de.ywegel.svenska.fakes.UserPreferencesManagerFake
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isGreaterThan
import strikt.assertions.isNotEmpty
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import java.io.IOException

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

    private fun observer(preferences: UserPreferencesManager, sentryController: SentryControllerFake) =
        SentryConsentObserver(
            preferences = preferences,
            sentryController = sentryController,
            getOrCreateCrashReportingId = GetOrCreateCrashReportingIdUseCase(preferences, testDispatcher),
        )

    @Test
    fun `sentry is not initialized while crash reporting is not enabled`() = runTest(testDispatcher) {
        // Given
        val sentryController = SentryControllerFake()
        val preferencesManager = UserPreferencesManagerFake()
        val observer = observer(preferencesManager, sentryController)

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
        val observer = observer(preferencesManager, sentryController)
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
        val observer = observer(preferencesManager, sentryController)
        observer.start(TestScope(testDispatcher))
        advanceUntilIdle()
        expectThat(sentryController.isInitialized).isTrue()

        // When
        preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, false)
        advanceUntilIdle()

        // Then
        expectThat(sentryController.isInitialized).isFalse()
    }

    @Test
    fun `sentry is initialized with a stable pseudonymous id across consent toggles`() = runTest(testDispatcher) {
        // Given
        val sentryController = SentryControllerFake()
        val preferencesManager = UserPreferencesManagerFake()
        observer(preferencesManager, sentryController).start(TestScope(testDispatcher))
        preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, true)
        advanceUntilIdle()
        val firstId = sentryController.initializedWithUserId
        expectThat(firstId).isNotNull().isNotEmpty()

        // When consent is revoked and granted again
        preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, false)
        advanceUntilIdle()
        preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, true)
        advanceUntilIdle()

        // Then the same id is reused, so earlier reports stay locatable for a deletion request
        expectThat(sentryController.initializedWithUserId).isEqualTo(firstId)
    }

    @Test
    fun `stays off when no id can be resolved, and still reacts to later consent changes`() = runTest(testDispatcher) {
        // Given a preferences store whose edit() always fails, as DataStore may on a corrupt file
        val sentryController = SentryControllerFake()
        val delegate = UserPreferencesManagerFake()
        val preferences = object : UserPreferencesManager by delegate {
            override suspend fun <S, V> edit(key: PreferenceKey<S, V>, transform: (V) -> V) {
                throw IOException("cannot read preferences")
            }
        }
        observer(preferences, sentryController).start(TestScope(testDispatcher))

        // When consent is granted and the id lookup blows up
        delegate.update(PrivacyPreferenceKeys.CrashReportingEnabled, true)
        advanceUntilIdle()

        // Then we fail closed rather than reporting without an id
        expectThat(sentryController.isInitialized).isFalse()

        // And a use-case failure has not killed the collector, so a later opt-out still arrives
        val shutdownsBefore = sentryController.shutdownCount
        delegate.update(PrivacyPreferenceKeys.CrashReportingEnabled, false)
        advanceUntilIdle()
        expectThat(sentryController.shutdownCount).isGreaterThan(shutdownsBefore)
    }

    @Test
    fun `sentry pseudonymous id stays saved even after disabling reports`() = runTest(testDispatcher) {
        // Given
        val sentryController = SentryControllerFake()
        val preferencesManager = UserPreferencesManagerFake()
        observer(preferencesManager, sentryController).start(TestScope(testDispatcher))
        preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, true)
        advanceUntilIdle()
        val savedId = sentryController.initializedWithUserId
        expectThat(savedId).isNotNull().isNotEmpty()

        // When disabling reporting
        preferencesManager.update(PrivacyPreferenceKeys.CrashReportingEnabled, false)
        advanceUntilIdle()

        // Then id stays saved and stays the same
        expectThat(sentryController.initializedWithUserId).isEqualTo(savedId)
        expectThat(preferencesManager.flow(PrivacyPreferenceKeys.CrashReportingEnabled).first()).isFalse()
    }
}
