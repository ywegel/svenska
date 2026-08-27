package de.ywegel.svenska.data.preferences.keys

import de.ywegel.svenska.data.preferences.PreferenceStore.Overview
import de.ywegel.svenska.data.preferences.booleanPreference
import de.ywegel.svenska.data.preferences.intPreference

object LegalPreferenceKeys {
    /**
     * The latest accepted privacy policy version. The higher, the newer.
     */
    val isLatestPrivacyPolicyAccepted = intPreference(
        store = Overview,
        name = "app_latest_accepted_privacy_policy_version",
        default = 0,
    )

    const val LATEST_PRIVACY_VERSION = 2

    val isSentryActivated = booleanPreference(
        store = Overview,
        name = "app_is_sentry_activated",
        default = false,
    )
}
