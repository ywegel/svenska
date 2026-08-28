package de.ywegel.svenska.domain

import de.ywegel.svenska.data.preferences.PreferenceStore
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import de.ywegel.svenska.data.preferences.set
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Enables/Disables sentry crash-reporting and save the consent timestamp. Wrapped in a UseCase, because it is used in
 * multiple places.
 */
class SetCrashReportingConsentUseCase @Inject constructor(
    private val preferences: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(enabled: Boolean) {
        withContext(ioDispatcher) {
            preferences.editTransaction(PreferenceStore.Settings) {
                this[PrivacyPreferenceKeys.CrashReportingEnabled] = enabled
                this[PrivacyPreferenceKeys.ConsentDecisionTimestamp] = System.currentTimeMillis().toString()
            }
        }
    }
}
