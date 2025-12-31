package de.ywegel.svenska.ui.settings

import de.ywegel.svenska.domain.search.OnlineSearchType
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
object SettingsCallbacksFake : SettingsCallbacks {
    override fun toggleOverviewShowCompactVocabularyItem(showCompactVocabularyItem: Boolean) {}
    override fun onOnlineSearchTypeSelected(onlineSearchType: OnlineSearchType) {}
    override fun updateUseNewQuiz(useNewQuiz: Boolean) {}
}
