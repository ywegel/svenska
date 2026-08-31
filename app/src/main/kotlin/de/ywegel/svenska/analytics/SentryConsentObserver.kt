package de.ywegel.svenska.analytics

import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import de.ywegel.svenska.domain.GetOrCreateCrashReportingIdUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Start/stop the Sentry SDK based on user decision.
 */
@Singleton
class SentryConsentObserver @Inject constructor(
    private val preferences: UserPreferencesManager,
    private val sentryController: SentryController,
    private val getOrCreateCrashReportingId: GetOrCreateCrashReportingIdUseCase,
) {
    fun start(scope: CoroutineScope) {
        preferences.flow(PrivacyPreferenceKeys.CrashReportingEnabled)
            .onEach { enabled ->
                // No id means we stay off, see GetOrCreateCrashReportingIdUseCase.
                val userId = if (enabled) getOrCreateCrashReportingId() else null

                if (userId != null) {
                    sentryController.initialize(userId)
                } else {
                    sentryController.shutdown()
                }
            }
            .launchIn(scope)
    }
}
