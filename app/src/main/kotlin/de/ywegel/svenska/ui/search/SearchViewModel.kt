package de.ywegel.svenska.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.SortOrder
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.LinkedList
import java.util.Queue
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: VocabularyRepository,
    private val userPreferencesManager: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private val containerId: Int? = savedStateHandle.navArgs<SearchScreenNavArgs>().containerId

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val preferencesSearchFlow = userPreferencesManager.preferencesSearchFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    val vocabularyFlow = _searchQuery.flatMapLatest {
        repository.getVocabularies(
            query = it,
            containerId = containerId,
            sortOrder = SortOrder.Created,
            reverse = false,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    init {
        observePreferences()
    }

    /**
     * Invoked, everytime the users input changes in the search field
     **/
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Invoked, when the user clicks the submit button in the search field or on the keyboard
     **/
    fun onSearch(query: String) {
        _searchQuery.value = query

        _uiState.update {
            val updated = it.lastSearchedItems
            if (updated.size == MAX_LAST_SEARCH_COUNT) {
                updated.poll()
            }
            updated.add(query)
            it.copy(lastSearchedItems = updated)
        }
        viewModelScope.launch(ioDispatcher) {
            userPreferencesManager.updateOverviewLastSearchedItems(
                (_uiState.value.lastSearchedItems as LinkedList<String>),
            )
        }
    }

    private fun observePreferences() = viewModelScope.launch(ioDispatcher) {
        launch {
            preferencesSearchFlow.collectLatest { preferences ->
                _uiState.update {
                    it.copy(
                        lastSearchedItems = preferences.lastSearchedItems,
                        onlineRedirectUrl = preferences.onlineRedirectType.toUrl(),
                        showCompactVocabularyItem = preferences.showCompactVocabularyItem,
                    )
                }
            }
        }
    }
}

data class SearchUiState(
    val lastSearchedItems: Queue<String> = LinkedList(),
    val showCompactVocabularyItem: Boolean = false,
    val showOnlineRedirectFirst: Boolean = false,
    val onlineRedirectUrl: String? = null,
)

private const val MAX_LAST_SEARCH_COUNT = 5
