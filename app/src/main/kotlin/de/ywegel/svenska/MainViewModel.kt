package de.ywegel.svenska

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.OnboardingPreferenceKeys
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    preferences: UserPreferencesManager,
) : ViewModel() {

    val onboardingState: StateFlow<OnboardingState> =
        preferences.flow(OnboardingPreferenceKeys.HasCompleted)
            .map { if (it) OnboardingState.Completed else OnboardingState.NotCompleted }
            .stateIn(viewModelScope, SharingStarted.Eagerly, OnboardingState.Loading)
}

sealed interface OnboardingState {
    data object Loading : OnboardingState
    data object Completed : OnboardingState
    data object NotCompleted : OnboardingState
}
