package de.ywegel.svenska.ui.privacy

import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
object PrivacyConsentCallbacksFake : PrivacyConsentCallbacks {
    override fun onPolicyAcknowledged() {}
    override fun onCrashReportingDecision(enabled: Boolean) {}
}
