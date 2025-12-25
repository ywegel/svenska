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
) : ViewModel(), SettingsCallbacks {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val userPreferencesFlow = preferencesManager.preferencesOverviewFlow
    private val searchPreferencesFlow = preferencesManager.preferencesSearchFlow
    private val appPreferencesFlow = preferencesManager.preferencesAppFlow

    init {
        observerPreferencesState()
    }

    override fun toggleOverviewShowCompactVocabularyItem(showCompactVocabularyItem: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            preferencesManager.showCompactVocabularyItem(showCompactVocabularyItem)
        }
    }

    override fun toggleUseNewQuiz(useNewQuiz: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            preferencesManager.toggleUsesNewQuiz(useNewQuiz)
        }
    }

    override fun onOnlineSearchTypeSelected(onlineSearchType: OnlineSearchType) {
        viewModelScope.launch(ioDispatcher) {
            preferencesManager.updateOnlineRedirectType(onlineSearchType)
        }
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
                        selectedOnlineSearchType = preferences.onlineRedirectType,
                    )
                }
            }
        }
        launch {
            appPreferencesFlow.collectLatest { preferences ->
                _uiState.update {
                    it.copy(appUseNewQuiz = preferences.useNewQuiz)
                }
            }
        }
    }
}

data class SettingsUiState(
    val overviewShowCompactVocabularyItem: Boolean = false,
    val appUseNewQuiz: Boolean = false,
    val selectedOnlineSearchType: OnlineSearchType? = null,
)
