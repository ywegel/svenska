package de.ywegel.svenska.ui.settings

import de.ywegel.svenska.domain.search.OnlineSearchType

/**
 * A callback interface to shorten the SettingsScreen parameter list
 */
interface SettingsCallbacks {
    fun toggleOverviewShowCompactVocabularyItem(showCompactVocabularyItem: Boolean)
    fun updateUseNewQuiz(useNewQuiz: Boolean)
    fun onOnlineSearchTypeSelected(onlineSearchType: OnlineSearchType)
}
