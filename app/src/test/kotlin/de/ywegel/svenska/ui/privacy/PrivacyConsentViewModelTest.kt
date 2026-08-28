@file:OptIn(ExperimentalCoroutinesApi::class)

package de.ywegel.svenska.ui.privacy

import app.cash.turbine.test
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import de.ywegel.svenska.domain.SetCrashReportingConsentUseCase
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
import strikt.assertions.isNotEmpty
import strikt.assertions.isTrue

class PrivacyConsentViewModelTest {

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
    fun `onPolicyAcknowledged persists the current policy version`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()
        val viewModel = PrivacyConsentViewModel(
            preferencesManager,
            SetCrashReportingConsentUseCase(preferencesManager, testDispatcher),
        )

        // When
        viewModel.onPolicyAcknowledged()
        advanceUntilIdle()

        // Then
        preferencesManager.flow(PrivacyPreferenceKeys.AcknowledgedPolicyVersion).test {
            expectThat(awaitItem()).isEqualTo(PrivacyPreferenceKeys.CURRENT_POLICY_VERSION)
        }
    }

    @Test
    fun `onCrashReportingDecision persists acceptance and a decision timestamp`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()
        val viewModel = PrivacyConsentViewModel(
            preferencesManager,
            SetCrashReportingConsentUseCase(preferencesManager, testDispatcher),
        )

        // When
        viewModel.onCrashReportingDecision(true)
        advanceUntilIdle()

        // Then
        preferencesManager.flow(PrivacyPreferenceKeys.CrashReportingEnabled).test {
            expectThat(awaitItem()).isTrue()
        }
        preferencesManager.flow(PrivacyPreferenceKeys.ConsentDecisionTimestamp).test {
            expectThat(awaitItem()).isNotEmpty()
        }
    }

    @Test
    fun `declining crash reporting still persists a decision timestamp`() = runTest(testDispatcher) {
        // Given
        val preferencesManager = UserPreferencesManagerFake()
        val viewModel = PrivacyConsentViewModel(
            preferencesManager,
            SetCrashReportingConsentUseCase(preferencesManager, testDispatcher),
        )

        // When
        viewModel.onCrashReportingDecision(false)
        advanceUntilIdle()

        // Then
        preferencesManager.flow(PrivacyPreferenceKeys.CrashReportingEnabled).test {
            expectThat(awaitItem()).isFalse()
        }
        preferencesManager.flow(PrivacyPreferenceKeys.ConsentDecisionTimestamp).test {
            expectThat(awaitItem()).isNotEmpty()
        }
    }
}
