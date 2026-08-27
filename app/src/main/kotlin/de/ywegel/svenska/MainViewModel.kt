package de.ywegel.svenska

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.LegalPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.OnboardingPreferenceKeys
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferences: UserPreferencesManager,
) : ViewModel() {

    val mainUiState: StateFlow<MainUiState> = combine(
        preferences.flow(OnboardingPreferenceKeys.HasCompleted),
        preferences.flow(LegalPreferenceKeys.isLatestPrivacyPolicyAccepted),
    ) { hasCompletedOnboarding, acceptedVersion ->
        MainUiState.Ready(
            hasCompletedOnboarding = hasCompletedOnboarding,
            isLatestPrivacyPolicyAccepted = acceptedVersion == LegalPreferenceKeys.LATEST_PRIVACY_VERSION,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainUiState.Loading)

    fun onPrivacyPolicyAccepted() = viewModelScope.launch {
        preferences.update(LegalPreferenceKeys.isLatestPrivacyPolicyAccepted, 2)
    }
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(
        val hasCompletedOnboarding: Boolean,
        val isLatestPrivacyPolicyAccepted: Boolean,
    ) : MainUiState
}
