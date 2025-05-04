package de.ywegel.svenska.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.domain.search.OnlineSearchType
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
    private val searchPreferencesFlow = preferencesManager.preferencesSearchFlow

    init {
        observerPreferencesState()
    }

    fun toggleOverviewShowCompactVocabularyItem(showCompactVocabularyItem: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            preferencesManager.showCompactVocabularyItem(showCompactVocabularyItem)
        }

    fun toggleSearchShowCompactVocabularyItem(showCompactVocabularyItem: Boolean) =
        viewModelScope.launch(ioDispatcher) {
            preferencesManager.showCompactVocabularyItemInSearch(showCompactVocabularyItem)
        }

    fun onOnlineSearchTypeSelected(onlineSearchType: OnlineSearchType) = viewModelScope.launch(ioDispatcher) {
        preferencesManager.updateOnlineRedirectType(onlineSearchType)
    }

    private fun observerPreferencesState() = viewModelScope.launch {
        launch {
            userPreferencesFlow.collectLatest { preferences ->
                _uiState.update {
                    it.copy(
                        overviewShowCompactVocabularyItem = preferences.showCompactVocabularyItem,
                    )
                }
            }
        }
        launch {
            searchPreferencesFlow.collectLatest { preferences ->
                _uiState.update {
                    it.copy(
                        searchShowCompactVocabularyItem = preferences.showCompactVocabularyItem,
                        selectedOnlineSearchType = preferences.onlineRedirectType,
                    )
                }
            }
        }
    }
}

data class SettingsUiState(
    val overviewShowCompactVocabularyItem: Boolean = false,
    val searchShowCompactVocabularyItem: Boolean = false,
    val selectedOnlineSearchType: OnlineSearchType? = null,
)
