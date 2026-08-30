package de.ywegel.svenska.ui.privacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import de.ywegel.svenska.domain.SetCrashReportingConsentUseCase
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacyConsentViewModel @Inject constructor(
    private val preferences: UserPreferencesManager,
    private val setCrashReportingConsent: SetCrashReportingConsentUseCase,
) : ViewModel(), PrivacyConsentCallbacks {

    override fun onPolicyAcknowledged() {
        viewModelScope.launch {
            preferences.update(
                PrivacyPreferenceKeys.AcknowledgedPolicyVersion,
                PrivacyPreferenceKeys.CURRENT_POLICY_VERSION,
            )
        }
    }

    override fun onCrashReportingDecision(enabled: Boolean) {
        viewModelScope.launch { setCrashReportingConsent(enabled) }
    }
}
