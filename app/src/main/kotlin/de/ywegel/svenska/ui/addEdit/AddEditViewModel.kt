package de.ywegel.svenska.ui.addEdit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.domain.addEdit.MapUiStateToVocabularyUseCase
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VocabularyRepository,
    private val mapUiStateToVocabularyUseCase: MapUiStateToVocabularyUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel(), AddEditVocabularyCallbacks {

    private var initialVocabulary = savedStateHandle.navArgs<AddEditNavArgs>().initialVocabulary
    private var containerId = savedStateHandle.navArgs<AddEditNavArgs>().containerId

    private val _uiState = MutableStateFlow(UiState.fromExistingVocabulary(initialVocabulary))
    val uiState = _uiState.asStateFlow()

    override fun updateSelectedWordGroup(group: ViewWordGroup) {
        _uiState.update {
            if(it.selectedWordGroup != group) {
                it.copy(selectedWordGroup = group, selectedSubGroup = ViewWordSubGroup.None)
            } else {
                it.copy(selectedWordGroup = group)
            }
        }
    }

    override fun updateSelectedSubWordGroup(subGroup: ViewWordSubGroup) {
        _uiState.update {
            it.copy(selectedSubGroup = subGroup)
        }
    }

    override fun updateGender(gender: Gender?) {
        _uiState.update {
            it.copy(gender = gender)
        }
    }

    override fun updateWordWithAnnotation(word: String) {
        _uiState.update {
            it.copy(wordWithAnnotation = word)
        }
    }

    override fun updateTranslation(translation: String) {
        _uiState.update {
            it.copy(translation = translation)
        }
    }

    override fun updateEnding(ending: String) {
        _uiState.update {
            it.copy(ending = ending)
        }
    }

    override fun updateNotes(notes: String) {
        _uiState.update {
            it.copy(notes = notes)
        }
    }

    override fun updateIsIrregularPronunciation(isIrregular: Boolean) {
        _uiState.update {
            it.copy(isIrregularPronunciation = isIrregular)
        }
    }

    override fun updateIrregularPronunciation(pronunciation: String) {
        _uiState.update {
            it.copy(irregularPronunciation = pronunciation)
        }
    }

    override fun updateIsFavorite(isFavorite: Boolean) {
        _uiState.update {
            it.copy(isFavorite = isFavorite)
        }
    }

    override fun deleteVocabulary(navigateUp: () -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            initialVocabulary?.let { repository.deleteVocabulary(it) }
        }
        navigateUp()
    }

    override fun saveAndNavigateUp(navigateUp: () -> Unit) {
        val vocab = mapUiStateToVocabularyUseCase(uiState.value, initialVocabulary, containerId)
            // TODO: Show error. This is maybe not necessary, if a popup is implemented, which indicates that no subGroup is set
            ?: return

        viewModelScope.launch(ioDispatcher) {
            repository.upsertVocabulary(vocab)
        }
        navigateUp() // TODO: Use viewmodel events instead of calling navigation method
    }
}

data class UiState(
    val selectedWordGroup: ViewWordGroup? = null,
    val selectedSubGroup: ViewWordSubGroup = ViewWordSubGroup.None,
    val gender: Gender? = null,
    val wordWithAnnotation: String = "",
    val translation: String = "",
    val ending: String = "",
    val notes: String = "",
    val isIrregularPronunciation: Boolean = false,
    val irregularPronunciation: String? = null,
    val isFavorite: Boolean = false,
    val editingExistingVocabulary: String? = null,
) {
    companion object {
        fun fromExistingVocabulary(vocabulary: Vocabulary?): UiState {
            return vocabulary?.let {
                UiState(
                    selectedWordGroup = ViewWordGroup.fromWordGroup(vocabulary.wordGroup),
                    selectedSubGroup = ViewWordSubGroup.fromWordGroup(vocabulary.wordGroup),
                    gender = vocabulary.gender,
                    wordWithAnnotation = vocabulary.word,
                    translation = vocabulary.translation,
                    ending = vocabulary.ending,
                    notes = vocabulary.notes,
                    isIrregularPronunciation = vocabulary.irregularPronunciation?.isNotBlank() == true,
                    irregularPronunciation = vocabulary.irregularPronunciation,
                    isFavorite = vocabulary.isFavorite,
                    editingExistingVocabulary = vocabulary.word,
                )
            } ?: UiState()
        }
    }
}
