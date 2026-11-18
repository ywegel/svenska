package de.ywegel.svenska.ui.addEdit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.di.ImmediateDispatcher
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.domain.words.WordGroupMatcher
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@Suppress("TooManyFunctions")
@HiltViewModel
class AddEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: VocabularyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @ImmediateDispatcher private val immediateDispatcher: CoroutineDispatcher,
) : ViewModel(), AddEditVocabularyCallbacks {

    private val initialVocabulary = savedStateHandle.navArgs<AddEditNavArgs>().initialVocabulary
    private val containerId = savedStateHandle.navArgs<AddEditNavArgs>().containerId

    private val _uiState = MutableStateFlow(AddEditUiState.fromExistingVocabulary(initialVocabulary))
    val uiState = _uiState.asStateFlow()

    private val _uiEvents = Channel<UiEvent>() // TODO: Rename to _uiEventsChannel once ktlint is upgraded to 1.3.1
    val uiEvents = _uiEvents.receiveAsFlow()

    override fun updateSelectedWordGroup(group: ViewWordGroup) {
        _uiState.update {
            if (it.selectedWordGroup != group) {
                it.copy(selectedWordGroup = group, selectedSubGroup = ViewWordSubGroup.None).withArticleGender()
            } else {
                it
            }
        }
    }

    override fun updateSelectedSubWordGroup(subGroup: ViewWordSubGroup) {
        _uiState.update { it.copy(selectedSubGroup = subGroup) }
    }

    override fun updateGender(gender: Gender) {
        _uiState.update { it.copy(gender = gender) }
    }

    override fun updateWordWithAnnotation(word: String) {
        _uiState.update { old ->
            val new = old.copy(wordWithAnnotation = word)
            // The chosen index would point to another word once words are added or removed
            val keepsChosenWord = new.wordInput.words.size == old.wordInput.words.size
            new.copy(chosenChangingIndex = old.chosenChangingIndex.takeIf { keepsChosenWord }).withArticleGender()
        }
    }

    override fun selectChangingWord(index: Int) {
        _uiState.update { it.copy(chosenChangingIndex = index) }
    }

    override fun updateTranslation(translation: String) {
        _uiState.update { it.copy(translation = translation) }
    }

    override fun updateEndings(endings: String) {
        _uiState.update { it.copy(endings = endings) }
    }

    override fun updateWrittenForm(index: Int, form: String) {
        _uiState.update { it.withWrittenForm(index, form) }
    }

    override fun switchToWrittenOutForms() {
        _uiState.update { it.copy(formsMode = FormsMode.WrittenOut, writtenForms = it.builtFormsForAllSlots()) }
    }

    override fun switchToEndings() {
        _uiState.update { it.copy(formsMode = FormsMode.Endings) }
    }

    override fun confirmForms() {
        _uiState.update { it.copy(confirmedFormInputs = it.formInputs) }
    }

    override fun applySuggestedWordGroup() {
        _uiState.update { state ->
            val suggestion = state.suggestedWordGroup ?: return@update state
            state.copy(
                selectedWordGroup = ViewWordGroup.fromWordGroup(suggestion),
                selectedSubGroup = ViewWordSubGroup.fromWordGroup(suggestion),
                gender = WordGroupMatcher.determineGender(suggestion, state.formInputs.endings) ?: state.gender,
            )
        }
    }

    override fun updateNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    override fun updateIrregularPronunciation(pronunciation: String) {
        _uiState.update { it.copy(irregularPronunciation = pronunciation) }
    }

    override fun updateIsFavorite(isFavorite: Boolean) {
        _uiState.update { it.copy(isFavorite = isFavorite) }
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
        when (val result = mapUiStateToVocabulary(uiState.value, initialVocabulary, containerId)) {
            is SaveResult.Valid -> viewModelScope.launch(ioDispatcher) {
                repository.upsertVocabulary(result.vocabulary)
                withContext(immediateDispatcher) {
                    _uiEvents.send(UiEvent.NavigateUp)
                }
            }
            SaveResult.MissingWordGroup -> viewModelScope.launch(immediateDispatcher) {
                _uiEvents.send(UiEvent.InvalidWordGroupConfiguration)
            }
            // The errors are shown under the word field and the forms
            SaveResult.InvalidHighlights, is SaveResult.InvalidWrittenForms -> Unit
        }
    }

    sealed interface UiEvent {
        data object NavigateUp : UiEvent
        data object InvalidWordGroupConfiguration : UiEvent
    }
}

internal fun AddEditUiState.withArticleGender(): AddEditUiState =
    wordInput.articleGender?.let { copy(gender = it) } ?: this

