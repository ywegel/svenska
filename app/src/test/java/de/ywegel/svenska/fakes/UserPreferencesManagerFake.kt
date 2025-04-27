package de.ywegel.svenska.fakes

import de.ywegel.svenska.data.SortOrder
import de.ywegel.svenska.data.preferences.OverviewPreferences
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import java.util.LinkedList
import java.util.Queue

class UserPreferencesManagerFake(
    initialSortOrder: SortOrder = SortOrder.default,
    initialRevert: Boolean = false,
) : UserPreferencesManager {

    private var currentPreferences = OverviewPreferences(
        sortOrder = initialSortOrder,
        revert = initialRevert,
        lastSearchedItems = LinkedList(),
        showCompactVocabularyItem = false,
    )
    private val preferencesFlow = MutableStateFlow(currentPreferences)

    override val preferencesOverviewFlow: Flow<OverviewPreferences> = preferencesFlow

    override suspend fun updateOverviewSortOrder(sortOrder: SortOrder) {
        preferencesFlow.update {
            it.copy(sortOrder = sortOrder)
        }
    }

    override suspend fun updateOverviewSortOrderRevert(revert: Boolean) {
        preferencesFlow.update {
            it.copy(revert = revert)
        }
    }

    override suspend fun updateOverviewLastSearchedItems(queue: Queue<String>) {
        preferencesFlow.update {
            it.copy(lastSearchedItems = queue)
        }
    }

    override suspend fun showCompactVocabularyItem(showCompactVocabularyItem: Boolean) {
        preferencesFlow.update {
            it.copy(showCompactVocabularyItem = showCompactVocabularyItem)
        }
    }
}
