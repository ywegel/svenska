package de.ywegel.svenska.ui.overview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.QuizPoolRepository
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.SortOrder
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.VocabularyWithPoolState
import de.ywegel.svenska.data.preferences.UserPreferencesManager
import de.ywegel.svenska.data.preferences.keys.AppPreferenceKeys
import de.ywegel.svenska.data.preferences.keys.OverviewPreferenceKeys
import de.ywegel.svenska.domain.ToggleVocabularyFavoriteUseCase
import de.ywegel.svenska.ui.common.vocabulary.VocabularyListCallbacks
import de.ywegel.svenska.ui.detail.VocabularyDetailState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val vocabularyRepository: VocabularyRepository,
    private val containerRepository: ContainerRepository,
    private val quizPoolRepository: QuizPoolRepository,
    private val toggleVocabularyFavoriteUseCase: ToggleVocabularyFavoriteUseCase,
    private val userPreferencesManager: UserPreferencesManager,
) : ViewModel(), VocabularyListCallbacks {

    val containerId: Int = savedStateHandle.navArgs<OverviewNavArgs>().containerId

    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState: StateFlow<OverviewUiState> = _uiState.asStateFlow()

    private val sortSettings = combine(
        userPreferencesManager.flow(OverviewPreferenceKeys.SortOrder),
        userPreferencesManager.flow(OverviewPreferenceKeys.Revert),
        ::Pair,
    )

    init {
        observeVocabularyState()
        observerPreferencesState()
        loadContainerName()
    }

    private fun loadContainerName() = viewModelScope.launch {
        val name = containerRepository.getContainerById(containerId)?.name.orEmpty()
        _uiState.update { it.copy(containerName = name) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeVocabularyState() = viewModelScope.launch {
        val vocabularies = sortSettings.flatMapLatest { (sortOrder, revert) ->
            vocabularyRepository.getVocabularies(
                containerId = containerId,
                sortOrder = sortOrder,
                reverse = revert,
            ).onStart { _uiState.update { it.copy(isLoading = true) } }
        }

        quizPoolRepository.withPoolStates(vocabularies)
            .collect { items ->
                _uiState.update { it.copy(items = items, isLoading = false) }
            }
    }

    private fun observerPreferencesState() = viewModelScope.launch {
        launch {
            sortSettings.collect { (sortOrder, revert) ->
                _uiState.update {
                    it.copy(sortOrder = sortOrder, isReverseSort = revert)
                }
            }
        }
        launch {
            userPreferencesManager.flow(OverviewPreferenceKeys.ShowCompactVocabularyItem).collect { value ->
                _uiState.update {
                    it.copy(showCompactVocabularyItem = value)
                }
            }
        }
        launch {
            userPreferencesManager.flow(AppPreferenceKeys.UseNewQuiz).collect { value ->
                _uiState.update {
                    it.copy(useNewQuiz = value)
                }
            }
        }
    }

    override fun onVocabularyClick(
        vocabulary: Vocabulary,
        showContainerInformation: Boolean, // We never show the container information in the overviewModel
    ) {
        _uiState.update {
            it.copy(
                detailViewState = VocabularyDetailState.Visible(
                    selectedVocabulary = vocabulary,
                    selectedVocabularyContainer = null,
                ),
            )
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
}

data class OverviewUiState(
    val items: List<VocabularyWithPoolState> = emptyList(),
    val containerName: String = "...",
    val isLoading: Boolean = true,
    val sortOrder: SortOrder = SortOrder.default,
    val isReverseSort: Boolean = false,
    val showSortDialog: Boolean = false,
    val showCompactVocabularyItem: Boolean = false,
    val detailViewState: VocabularyDetailState = VocabularyDetailState.Hidden,
    val useNewQuiz: Boolean = false,
)
