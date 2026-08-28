package de.ywegel.svenska.analytics

import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
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
) {
    fun start(scope: CoroutineScope) {
        preferences.flow(PrivacyPreferenceKeys.CrashReportingEnabled)
            .onEach { enabled -> if (enabled) sentryController.initialize() else sentryController.shutdown() }
            .launchIn(scope)
    }
}
