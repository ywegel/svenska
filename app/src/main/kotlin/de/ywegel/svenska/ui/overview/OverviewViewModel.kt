package de.ywegel.svenska.ui.overview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.SortOrder
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VocabularyRepository,
    userPreferencesManager: UserPreferencesManager,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val containerId: Int = savedStateHandle.navArgs<OverviewNavArgs>().containerId

    private val userPreferencesFlow = userPreferencesManager.preferencesOverviewFlow

    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    init {
        observeVocabularyState()
        observerPreferencesState()
    }

//    @OptIn(ExperimentalCoroutinesApi::class)
//    private fun observeVocabularyState() = viewModelScope.launch(ioDispatcher) {
//        combine(
//            userPreferencesFlow,
//            _searchQuery,
//        ) { preferences, query ->
//            Pair(preferences, query)
//        }
//            .onStart {
//                _uiState.update {
//                    it.copy(vocabulary = emptyList(), isLoading = true)
//                }
//            }
//            .flatMapLatest { (preferences, searchQuery) ->
//                repository.getVocabulariesByContainer(
//                    query = searchQuery,
//                    containerId = containerId,
//                    sortOrder = preferences.sortOrder,
//                    reverse = preferences.revert
//                )
//            }
//            .collectLatest { emergencies ->
//                _uiState.update {
//                    it.copy(vocabulary = emergencies, isLoading = false)
//                }
//            }
//    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeVocabularyState() = viewModelScope.launch(ioDispatcher) {
        userPreferencesFlow
            .onStart {
                _uiState.update {
                    it.copy(vocabulary = emptyList(), isLoading = true)
                }
            }
            .flatMapLatest { preferences ->
                // TODO: maybe replace with a search without query
                repository.getVocabularies(
                    query = "",
                    containerId = containerId,
                    sortOrder = preferences.sortOrder,
                    reverse = preferences.revert,
                )
            }
            .collectLatest { emergencies ->
                _uiState.update {
                    it.copy(vocabulary = emergencies, isLoading = false)
                }
            }
    }

    private fun observerPreferencesState() = viewModelScope.launch {
        launch {
            userPreferencesFlow.collectLatest { preferences ->
                _uiState.update {
                    it.copy(
                        sortOrder = preferences.sortOrder,
                        isReverseSort = preferences.revert,
                        showCompactVocabularyItem = preferences.showCompactVocabularyItem,
                    )
                }
            }
        }
    }

    fun showVocabularyDetail(vocabulary: Vocabulary) {
        _uiState.update {
            it.copy(
                selectedVocabulary = vocabulary,
                showDetailScreen = true,
            )
        }
    }

    fun hideVocabularyDetail() {
        _uiState.update {
            it.copy(
                showDetailScreen = false,
            )
        }
    }
}

data class OverviewUiState(
    val vocabulary: List<Vocabulary> = emptyList(),
    val isLoading: Boolean = true,
    val sortOrder: SortOrder = SortOrder.default,
    val isReverseSort: Boolean = false,
    val showSortDialog: Boolean = false,
    val showCompactVocabularyItem: Boolean = false,
    val showDetailScreen: Boolean = false,
    val selectedVocabulary: Vocabulary? = null,
)
