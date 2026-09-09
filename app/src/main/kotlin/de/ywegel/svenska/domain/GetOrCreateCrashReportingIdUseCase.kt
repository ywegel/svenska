package de.ywegel.svenska.domain

import android.util.Log
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.UUID
import javax.inject.Inject

/**
 * Returns the pseudonymous id that every crash report is tagged with. Creates it on first use.
 *
 * Null if DataStore is unreadable. Reporting under no id would leave the user unable to ask for those
 * reports to be deleted, so callers have to keep crash reporting off instead.
 */
class GetOrCreateCrashReportingIdUseCase @Inject constructor(
    private val preferences: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(): String? = withContext(ioDispatcher) {
        try {
            // edit() rather than read-then-write, so two callers cannot generate competing ids.
            preferences.edit(PrivacyPreferenceKeys.CrashReportingId) { current ->
                current.ifEmpty { "$ID_PREFIX${UUID.randomUUID()}" }
            }
            // Empty means the read fell back to the key's default, so treat it as a failure too.
            preferences.flow(PrivacyPreferenceKeys.CrashReportingId).first().ifEmpty { null }
        } catch (e: IOException) {
            Log.e(TAG, "Could not persist the crash reporting id", e)
            null
        }
    }
}

private const val TAG = "CrashReportingId"

private const val ID_PREFIX = "svenska-"
