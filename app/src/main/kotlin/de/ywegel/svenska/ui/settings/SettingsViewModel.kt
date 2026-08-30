package de.ywegel.svenska.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.model.OnlineSearchType
import de.ywegel.svenska.data.preferences.PreferenceKey
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.AppPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.OverviewPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.PrivacyPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.SearchPreferenceKeys
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.domain.SetCrashReportingConsentUseCase
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
    private val setCrashReportingConsent: SetCrashReportingConsentUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), SettingsCallbacks {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.flow(OverviewPreferenceKeys.ShowCompactVocabularyItem),
        preferencesManager.flow(AppPreferenceKeys.UseNewQuiz),
        preferencesManager.flow(SearchPreferenceKeys.OnlineRedirectType),
        preferencesManager.flow(PrivacyPreferenceKeys.CrashReportingEnabled),
        preferencesManager.flow(PrivacyPreferenceKeys.ConsentDecisionTimestamp),
    ) { showCompactVocabularyItem, useNewQuiz, onlineSearchType, crashReportingEnabled, consentTimestamp ->
        SettingsUiState(
            overviewShowCompactVocabularyItem = showCompactVocabularyItem,
            appUseNewQuiz = useNewQuiz,
            selectedOnlineSearchType = onlineSearchType,
            crashReportingEnabled = crashReportingEnabled,
            crashReportingConsentTimestamp = consentTimestamp.toLongOrNull(),
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

    override fun updateCrashReportingEnabled(enabled: Boolean) {
        viewModelScope.launch { setCrashReportingConsent(enabled) }
    }

    private fun <S, V> set(key: PreferenceKey<S, V>, value: V) = viewModelScope.launch(ioDispatcher) {
        preferencesManager.update(key, value)
    }
}

data class SettingsUiState(
    val overviewShowCompactVocabularyItem: Boolean = false,
    val appUseNewQuiz: Boolean = false,
    val selectedOnlineSearchType: OnlineSearchType? = null,
    val crashReportingEnabled: Boolean = false,
    val crashReportingConsentTimestamp: Long? = null,
)
