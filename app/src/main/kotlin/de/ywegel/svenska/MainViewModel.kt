package de.ywegel.svenska

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.OnboardingPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    preferences: UserPreferencesManager,
) : ViewModel() {

    val mainUiState: StateFlow<MainUiState> = combine(
        preferences.flow(OnboardingPreferenceKeys.HasCompleted),
        preferences.flow(PrivacyPreferenceKeys.AcknowledgedPolicyVersion),
        preferences.flow(PrivacyPreferenceKeys.ConsentDecisionTimestamp),
    ) { hasCompletedOnboarding, acknowledgedPolicyVersion, consentDecisionTimestamp ->
        val consentStep = when {
            acknowledgedPolicyVersion != PrivacyPreferenceKeys.CURRENT_POLICY_VERSION -> ConsentStep.Policy
            consentDecisionTimestamp.isEmpty() -> ConsentStep.CrashReporting
            else -> ConsentStep.Done
        }
        MainUiState.Ready(hasCompletedOnboarding = hasCompletedOnboarding, consentStep = consentStep)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState.Loading)
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(
        val hasCompletedOnboarding: Boolean,
        val consentStep: ConsentStep,
    ) : MainUiState
}

/**
 * Resolution: acknowledgedPolicyVersion != CURRENT_POLICY_VERSION -> Policy; acknowledged but no
 * decision timestamp recorded -> CrashReporting; otherwise -> Done.
 */
sealed interface ConsentStep {
    data object Policy : ConsentStep
    data object CrashReporting : ConsentStep
    data object Done : ConsentStep
}
