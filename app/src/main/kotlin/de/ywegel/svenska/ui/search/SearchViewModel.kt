package de.ywegel.svenska.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.SearchRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.domain.ToggleVocabularyFavoriteUseCase
import de.ywegel.svenska.ui.common.vocabulary.VocabularyListCallbacks
import de.ywegel.svenska.ui.detail.VocabularyDetailState
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
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val toggleVocabularyFavoriteUseCase: ToggleVocabularyFavoriteUseCase,
    private val containerRepository: ContainerRepository,
    private val searchRepository: SearchRepository,
    private val userPreferencesManager: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), VocabularyListCallbacks {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState = _uiState.asStateFlow()

    private val containerId: Int? = savedStateHandle.navArgs<SearchScreenNavArgs>().containerId

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val preferencesSearchFlow = userPreferencesManager.preferencesSearchFlow

    @OptIn(ExperimentalCoroutinesApi::class)
    val vocabularyFlow = _searchQuery.flatMapLatest {
        searchRepository.searchVocabularies(
            query = it,
            containerId = containerId,
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

        viewModelScope.launch(ioDispatcher) {
            userPreferencesManager.addLastSearchedItem(query)
        }
    }

    override fun onVocabularyClick(vocabulary: Vocabulary, showContainerInformation: Boolean) {
        _uiState.update {
            it.copy(
                detailViewState = VocabularyDetailState.Visible(
                    selectedVocabulary = vocabulary,
                    selectedVocabularyContainer = null,
                ),
            )
        }
        if (showContainerInformation) {
            viewModelScope.launch {
                val container = containerRepository.getContainerById(vocabulary.containerId)
                _uiState.update {
                    it.copy(
                        detailViewState = VocabularyDetailState.Visible(
                            selectedVocabulary = vocabulary,
                            selectedVocabularyContainer = container,
                        ),
                    )
                }
            }
        }
    }

    override fun onDismissVocabularyDetail() {
        _uiState.update {
            it.copy(
                detailViewState = VocabularyDetailState.Hidden,
            )
        }
    }

    override fun toggleFavorite(vocabularyId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            toggleVocabularyFavoriteUseCase(vocabularyId, isFavorite)
        }
    }

    private fun observePreferences() = viewModelScope.launch(ioDispatcher) {
        launch {
            preferencesSearchFlow.collectLatest { preferences ->
                _uiState.update {
                    it.copy(
                        lastSearchedItems = preferences.lastSearchedItems,
                        onlineRedirectUrl = preferences.onlineRedirectType.toUrl(),
                    )
                }
            }
        }
    }
}

data class SearchUiState(
    val lastSearchedItems: ArrayDeque<String> = ArrayDeque(),
    val showOnlineRedirectFirst: Boolean = false,
    val onlineRedirectUrl: String? = null,
    val detailViewState: VocabularyDetailState = VocabularyDetailState.Hidden,
)
