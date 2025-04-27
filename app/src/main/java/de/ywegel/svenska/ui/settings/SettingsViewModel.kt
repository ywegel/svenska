package de.ywegel.svenska.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val userPreferencesFlow = preferencesManager.preferencesOverviewFlow

    init {
        observerPreferencesState()
    }

    fun toggleShowCompactVocabularyItem(showCompactVocabularyItem: Boolean) = viewModelScope.launch(ioDispatcher) {
        preferencesManager.showCompactVocabularyItem(showCompactVocabularyItem)
    }

    private fun observerPreferencesState() = viewModelScope.launch {
        launch {
            userPreferencesFlow.collectLatest { preferences ->
                _uiState.update {
                    it.copy(
                        showCompactVocabularyItem = preferences.showCompactVocabularyItem,
                    )
                }
            }
        }
    }
}

data class SettingsUiState(
    val showCompactVocabularyItem: Boolean = false,
)
