package de.ywegel.svenska.ui.bonus.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.ContainerRepository
import de.ywegel.svenska.data.FavoritesAndPronunciationsRepository
import de.ywegel.svenska.data.QuizPoolRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.VocabularyWithPoolState
import de.ywegel.svenska.domain.ToggleVocabularyFavoriteUseCase
import de.ywegel.svenska.ui.common.WhileUiSubscribed
import de.ywegel.svenska.ui.common.vocabulary.VocabularyListCallbacks
import de.ywegel.svenska.ui.container.BonusScreen
import de.ywegel.svenska.ui.detail.VocabularyDetailState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesAndPronunciationViewModel @Inject constructor(
    private val containerRepository: ContainerRepository,
    favoritesAndPronunciationsRepository: FavoritesAndPronunciationsRepository,
    private val toggleVocabularyFavoriteUseCase: ToggleVocabularyFavoriteUseCase,
    quizPoolRepository: QuizPoolRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel(), VocabularyListCallbacks {

    val navArgs = savedStateHandle.navArgs<FavoritesAndPronunciationScreenNavArgs>()


    private val detailState = MutableStateFlow<VocabularyDetailState>(VocabularyDetailState.Hidden)

    private val vocabularies = when (navArgs.screenType) {
        BonusScreen.Favorites -> favoritesAndPronunciationsRepository.getFavorites(null)
        BonusScreen.SpecialPronunciation -> favoritesAndPronunciationsRepository.getPronunciations(null)
        else -> flow { error("This screen type is not supported") }
    }

    val uiState: StateFlow<FavoritesUiState> =
        combine(quizPoolRepository.withPoolStates(vocabularies), detailState) { items, detail ->
            FavoritesUiState.Success(items, detail)
        }
            .catch<FavoritesUiState> { emit(FavoritesUiState.Error(it.message ?: "Unknown error")) }
            .stateIn(
                scope = viewModelScope,
                started = WhileUiSubscribed,
                initialValue = FavoritesUiState.Loading,
            )

    override fun onVocabularyClick(vocabulary: Vocabulary, showContainerInformation: Boolean) {
        detailState.value = VocabularyDetailState.Visible(
            selectedVocabulary = vocabulary,
            selectedVocabularyContainer = null,
        )

        if (showContainerInformation) {
            viewModelScope.launch {
                val container = containerRepository.getContainerById(vocabulary.containerId)
                detailState.update { current ->
                    if (current is VocabularyDetailState.Visible && current.selectedVocabulary.id == vocabulary.id) {
                        current.copy(selectedVocabularyContainer = container)
                    } else {
                        current
                    }
                }
            }
        }
    }

    override fun onDismissVocabularyDetail() {
        detailState.value = VocabularyDetailState.Hidden
    }

    override fun toggleFavorite(vocabularyId: Int, isFavorite: Boolean) {
        viewModelScope.launch {
            toggleVocabularyFavoriteUseCase(vocabularyId, isFavorite)
        }
    }
}

sealed class FavoritesUiState {
    data object Loading : FavoritesUiState()
    data class Success(
        val items: List<VocabularyWithPoolState>,
        val detailViewState: VocabularyDetailState = VocabularyDetailState.Hidden,
    ) : FavoritesUiState()

    data class Error(val message: String) : FavoritesUiState()
}
