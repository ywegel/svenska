package de.ywegel.svenska.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.model.OnlineSearchType
import de.ywegel.svenska.data.preferences.PreferenceKey
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.AppPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.OverviewPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.SearchPreferenceKeys
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), SettingsCallbacks {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.flow(OverviewPreferenceKeys.ShowCompactVocabularyItem),
        preferencesManager.flow(AppPreferenceKeys.UseNewQuiz),
        preferencesManager.flow(SearchPreferenceKeys.OnlineRedirectType),
    ) { showCompactVocabularyItem, useNewQuiz, onlineSearchType ->
        SettingsUiState(
            overviewShowCompactVocabularyItem = showCompactVocabularyItem,
            appUseNewQuiz = useNewQuiz,
            selectedOnlineSearchType = onlineSearchType,
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SettingsUiState())

    override fun toggleOverviewShowCompactVocabularyItem(showCompactVocabularyItem: Boolean) {
        set(OverviewPreferenceKeys.ShowCompactVocabularyItem, showCompactVocabularyItem)
    }

    override fun updateUseNewQuiz(useNewQuiz: Boolean) {
        set(AppPreferenceKeys.UseNewQuiz, useNewQuiz)
    }

    override fun onOnlineSearchTypeSelected(onlineSearchType: OnlineSearchType) {
        set(SearchPreferenceKeys.OnlineRedirectType, onlineSearchType)
    }

    private fun <S, V> set(key: PreferenceKey<S, V>, value: V) = viewModelScope.launch(ioDispatcher) {
        preferencesManager.update(key, value)
    }
}

data class SettingsUiState(
    val overviewShowCompactVocabularyItem: Boolean = false,
    val appUseNewQuiz: Boolean = false,
    val selectedOnlineSearchType: OnlineSearchType? = null,
)
