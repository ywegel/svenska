package de.ywegel.svenska.ui.privacy

/**
 * A callback interface to shorten the PrivacyConsentSheet parameter list
 */
interface PrivacyConsentCallbacks {
    fun onPolicyAcknowledged()
    fun onCrashReportingDecision(enabled: Boolean)
}
