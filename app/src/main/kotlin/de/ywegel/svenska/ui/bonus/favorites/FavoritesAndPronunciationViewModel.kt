package de.ywegel.svenska.ui.bonus.favorites

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.ui.container.BonusScreen
import de.ywegel.svenska.ui.detail.VocabularyDetailState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesAndPronunciationViewModel @Inject constructor(
    private val repository: VocabularyRepository,
    savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    val navArgs = savedStateHandle.navArgs<FavoritesAndPronunciationScreenNavArgs>()

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = FavoritesUiState.Loading
            val result = when (navArgs.screenType) {
                BonusScreen.Favorites -> runCatching { repository.getFavorites(null) }
                BonusScreen.SpecialPronunciation -> runCatching { repository.getPronunciations(null) }
                else -> Result.failure(
                    // This can't happen, as you can only navigate to FavoritesAndPronunciationScreen for Favorites or Pronunciation
                    IllegalStateException("This screen type is not supported"),
                )
            }

            result
                .onSuccess { _uiState.value = FavoritesUiState.Success(it) }
                .onFailure { _uiState.value = FavoritesUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun showVocabularyDetail(vocabulary: Vocabulary) {
        _uiState.update {
            if (it is FavoritesUiState.Success) {
                it.copy(
                    detailViewState = VocabularyDetailState.Visible(vocabulary),
                )
            } else {
                it
            }
        }
    }

    fun hideVocabularyDetail() {
        _uiState.update {
            if (it is FavoritesUiState.Success) {
                it.copy(
                    detailViewState = VocabularyDetailState.Hidden,
                )
            } else {
                it
            }
        }
    }
}

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    data class Success(
        val items: List<Vocabulary>,
        val detailViewState: VocabularyDetailState = VocabularyDetailState.Hidden,
    ) : FavoritesUiState()

    data class Error(val message: String) : FavoritesUiState()
}
