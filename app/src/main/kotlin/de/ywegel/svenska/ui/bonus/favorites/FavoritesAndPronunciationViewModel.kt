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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesAndPronunciationViewModel @Inject constructor(
    private val repository: VocabularyRepository,
    private val savedStateHandle: SavedStateHandle,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val navArgs = savedStateHandle.navArgs<FavoritesAndPronunciationScreenNavArgs>()

    private val _bonusItems = MutableStateFlow<List<Vocabulary>>(emptyList())
    val bonusItems = _bonusItems.asStateFlow()

    init {
        viewModelScope.launch {
            when (navArgs.screenType) {
                BonusScreen.Favorites -> loadFavorites()
                BonusScreen.SpecialPronunciation -> loadPronunciations()
                else -> throw IllegalStateException(
                    "This can't happen, as you can only navigate to FavoritesAndPronunciationScreen" +
                        " for Favorites or Pronunciation",
                )
            }
        }
    }

    private fun loadFavorites() = viewModelScope.launch(ioDispatcher) {
        val favorites = repository.getFavorites(null)
        _bonusItems.update { favorites }
    }

    private fun loadPronunciations() = viewModelScope.launch(ioDispatcher) {
        val pronunciations = repository.getPronunciations(null)
        _bonusItems.update { pronunciations }
    }
}
