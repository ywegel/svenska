package de.ywegel.svenska

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.domain.main.AcceptLatestPrivacyPolicyUseCase
import de.ywegel.svenska.domain.main.LoadInitialAppStateUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val loadInitialAppState: LoadInitialAppStateUseCase,
    private val acceptLatestPrivacyPolicy: AcceptLatestPrivacyPolicyUseCase,
) : ViewModel() {

    private val _mainUiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val mainUiState: StateFlow<MainUiState> = _mainUiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadInitialAppState()
                .collect { appState ->
                    _mainUiState.value = MainUiState.Ready(
                        hasCompletedOnboarding = appState.hasCompletedOnboarding,
                        isLatestPrivacyPolicyAccepted = appState.isLatestPrivacyPolicyAccepted,
                    )
                }
        }
    }

    fun onPrivacyPolicyAccepted() = viewModelScope.launch {
        acceptLatestPrivacyPolicy()
    }
}

sealed interface MainUiState {
    data object Loading : MainUiState
    data class Ready(
        val hasCompletedOnboarding: Boolean,
        val isLatestPrivacyPolicyAccepted: Boolean,
    ) : MainUiState
}
