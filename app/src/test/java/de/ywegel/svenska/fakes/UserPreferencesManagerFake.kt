package de.ywegel.svenska.fakes

import de.ywegel.svenska.data.SortOrder
import de.ywegel.svenska.data.preferences.OverviewPreferences
import de.ywegel.svenska.data.preferences.SearchPreferences
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.LinkedList
import java.util.Queue

class UserPreferencesManagerFake(
    initialSortOrder: SortOrder = SortOrder.default,
    initialRevert: Boolean = false,
) : UserPreferencesManager {

    private var currentOverviewPreferences = OverviewPreferences(
        sortOrder = initialSortOrder,
        revert = initialRevert,
        showCompactVocabularyItem = false,
    )

    private val _preferencesOverviewFlow = MutableStateFlow(currentOverviewPreferences)

    override val preferencesOverviewFlow: Flow<OverviewPreferences> = _preferencesOverviewFlow

    override suspend fun updateOverviewSortOrder(sortOrder: SortOrder) {
        _preferencesOverviewFlow.update {
            it.copy(sortOrder = sortOrder)
        }
    }

    override suspend fun updateOverviewSortOrderRevert(revert: Boolean) {
        _preferencesOverviewFlow.update {
            it.copy(revert = revert)
        }
    }

    override suspend fun showCompactVocabularyItem(showCompactVocabularyItem: Boolean) {
        _preferencesOverviewFlow.update {
            it.copy(showCompactVocabularyItem = showCompactVocabularyItem)
        }
    }

    private val currentSearchPreferences = SearchPreferences(
        lastSearchedItems = LinkedList(),
        showOnlineRedirectFirst = false,
        showCompactVocabularyItem = false,
    )

    private val _preferencesSearchFlow = MutableStateFlow(currentSearchPreferences)

    override val preferencesSearchFlow: Flow<SearchPreferences> = _preferencesSearchFlow.asStateFlow()

    override suspend fun updateOverviewLastSearchedItems(queue: Queue<String>) {
        _preferencesSearchFlow.update {
            it.copy(lastSearchedItems = queue)
        }
    }

    override suspend fun showCompactVocabularyItemInSearch(show: Boolean) {
        _preferencesSearchFlow.update {
            it.copy(showCompactVocabularyItem = show)
        }
    }

    override suspend fun updateOnlineRedirectPosition(first: Boolean) {
        _preferencesSearchFlow.update {
            it.copy(showOnlineRedirectFirst = first)
        }
    }
}
