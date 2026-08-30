package de.ywegel.svenska.data.preferences.keys

import de.ywegel.svenska.data.preferences.PreferenceStore.Settings
import de.ywegel.svenska.data.preferences.booleanPreference
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys.CURRENT_POLICY_VERSION
import de.ywegel.svenska.data.preferences.stringPreference

object PrivacyPreferenceKeys {
    /**
     * The privacy policy version the user has acknowledged. Compared against [CURRENT_POLICY_VERSION]
     * to decide whether the user has to agree to the new policy.
     */
    val AcknowledgedPolicyVersion = stringPreference(
        store = Settings,
        name = "privacy_acknowledged_policy_version",
        default = "",
    )

    val CrashReportingEnabled = booleanPreference(
        store = Settings,
        name = "privacy_crash_reporting_enabled",
        default = false,
    )

    /**
     * Empty until the user has answered the crash-reporting question. Presence of this
     * timestamp is what marks the consent step as resolved.
     */
    val ConsentDecisionTimestamp = stringPreference(
        store = Settings,
        name = "privacy_consent_decision_timestamp",
        default = "",
    )

    /**
     * Only raise this, if the privacy policy changed. This automatically prompts the user.
     */
    const val CURRENT_POLICY_VERSION = "2026-08-2"
}
