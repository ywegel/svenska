package de.ywegel.svenska.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VocabularyDetailViewModel @Inject constructor(
    private val repository: VocabularyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    fun toggleFavorite(vocabularyId: Int, isFavorite: Boolean) {
        viewModelScope.launch(ioDispatcher) {
            repository.toggleVocabularyFavorite(vocabularyId, isFavorite)
        }
    }
}
