package de.ywegel.svenska.ui.addEdit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.di.ImmediateDispatcher
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.domain.addEdit.MapUiStateToVocabularyUseCase
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import de.ywegel.svenska.ui.common.vocabulary.HighlightUtils
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VocabularyRepository,
    private val mapUiStateToVocabularyUseCase: MapUiStateToVocabularyUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ImmediateDispatcher private val immediateDispatcher: CoroutineDispatcher,
) : ViewModel(), AddEditVocabularyCallbacks {

    private var initialVocabulary = savedStateHandle.navArgs<AddEditNavArgs>().initialVocabulary
    private var containerId = savedStateHandle.navArgs<AddEditNavArgs>().containerId

    private val _uiState = MutableStateFlow(AddEditUiState.fromExistingVocabulary(initialVocabulary))
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = Channel<UiEvent>() // TODO: Rename to _uiEventsChannel once ktlint is upgraded to 1.3.1
    val uiEvents = _uiEvents.receiveAsFlow()

    override fun updateSelectedWordGroup(group: ViewWordGroup) {
        _uiState.update {
            if (it.selectedWordGroup != group) {
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

    override fun deleteVocabulary() {
        viewModelScope.launch(ioDispatcher) {
            initialVocabulary?.let { repository.deleteVocabulary(it) }
            withContext(immediateDispatcher) {
                _uiEvents.send(UiEvent.NavigateUp)
            }
        }
    }

    override fun saveAndNavigateUp() {
        val vocab = mapUiStateToVocabularyUseCase(uiState.value, initialVocabulary, containerId)
            ?: run {
                viewModelScope.launch(immediateDispatcher) {
                    _uiEvents.send(UiEvent.InvalidWordGroupConfiguration)
                }
                return
            }

        viewModelScope.launch(ioDispatcher) {
            repository.upsertVocabulary(vocab)
            withContext(immediateDispatcher) {
                _uiEvents.send(UiEvent.NavigateUp)
            }
        }
    }

    sealed interface UiEvent {
        data object NavigateUp : UiEvent
        data object InvalidWordGroupConfiguration : UiEvent
    }
}

data class AddEditUiState(
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
        fun fromExistingVocabulary(vocabulary: Vocabulary?): AddEditUiState {
            return vocabulary?.let {
                AddEditUiState(
                    selectedWordGroup = ViewWordGroup.fromWordGroup(vocabulary.wordGroup),
                    selectedSubGroup = ViewWordSubGroup.fromWordGroup(vocabulary.wordGroup),
                    gender = vocabulary.gender,
                    wordWithAnnotation = HighlightUtils.reconstructWithStars(
                        vocabulary.word,
                        vocabulary.wordHighlights,
                    ),
                    translation = vocabulary.translation,
                    ending = vocabulary.ending,
                    notes = vocabulary.notes,
                    isIrregularPronunciation = vocabulary.irregularPronunciation?.isNotBlank() == true,
                    irregularPronunciation = vocabulary.irregularPronunciation,
                    isFavorite = vocabulary.isFavorite,
                    editingExistingVocabulary = vocabulary.word,
                )
            } ?: AddEditUiState()
        }
    }
}
