package de.ywegel.svenska.ui.addEdit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramcosta.composedestinations.generated.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import de.ywegel.svenska.data.VocabularyRepository
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.extractAnnotations
import de.ywegel.svenska.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: VocabularyRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private var initialVocabulary = savedStateHandle.navArgs<AddEditNavArgs>().initialVocabulary
    private var containerId = savedStateHandle.navArgs<AddEditNavArgs>().containerId

    private val _uiState = MutableStateFlow(UiState.fromExistingVocabulary(initialVocabulary))
    val uiState = _uiState.asStateFlow()

    fun updateSelectedWordGroup(group: WordGroup) {
        _uiState.update {
            it.copy(selectedWordGroup = group)
        }
    }

    fun updateGender(gender: Gender?) {
        _uiState.update {
            it.copy(gender = gender)
        }
    }

    fun updateWordWithAnnotation(word: String) {
        _uiState.update {
            it.copy(wordWithAnnotation = word)
        }
    }

    fun updateTranslation(translation: String) {
        _uiState.update {
            it.copy(translation = translation)
        }
    }

    fun updateEnding(ending: String) {
        _uiState.update {
            it.copy(ending = ending)
        }
    }

    fun updateNotes(notes: String) {
        _uiState.update {
            it.copy(notes = notes)
        }
    }

    fun updateIsIrregularPronunciation(isIrregular: Boolean) {
        _uiState.update {
            it.copy(isIrregularPronunciation = isIrregular)
        }
    }

    fun updateIsFavorite(isFavorite: Boolean) {
        _uiState.update {
            it.copy(isFavorite = isFavorite)
        }
    }

    fun deleteVocabulary(navigateUp: () -> Unit) {
        viewModelScope.launch(ioDispatcher) {
            initialVocabulary?.let { repository.deleteVocabulary(it) }
        }
        navigateUp()
    }

    fun saveAndGoBack(navigateUp: () -> Unit) {
        val snapshot = _uiState.value

        // TODO: definitely test this: If no gender was specified but a noun was selected, the dropdown is shown with a default value. This forces a default value, which should be reflected here
        val gender =
            if (snapshot.selectedWordGroup is WordGroup.Noun && snapshot.gender == null) {
                Gender.defaultIfEmpty
            } else {
                snapshot.gender
            }

        val (annotations, cleanString) = extractAnnotations(snapshot.wordWithAnnotation)

        val irregularPronunciation =
            snapshot.irregularPronunciation.takeIf { snapshot.isIrregularPronunciation }

        val new = initialVocabulary?.copy(
            word = cleanString,
            wordHighlights = annotations,
            translation = snapshot.translation,
            gender = gender,
            wordGroup = snapshot.selectedWordGroup,
            ending = snapshot.ending,
            irregularPronunciation = irregularPronunciation,
            isFavorite = snapshot.isFavorite,
            notes = snapshot.notes,
        ) ?: Vocabulary(
            word = cleanString,
            wordHighlights = annotations,
            translation = snapshot.translation,
            gender = gender,
            wordGroup = snapshot.selectedWordGroup,
            ending = snapshot.ending,
            notes = snapshot.notes,
            irregularPronunciation = irregularPronunciation,
            isFavorite = snapshot.isFavorite,
            containerId = containerId,
        )
        viewModelScope.launch(ioDispatcher) {
            repository.upsertVocabulary(new)
        }
        navigateUp()
    }
}

data class UiState(
    val selectedWordGroup: WordGroup = WordGroup.default,
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
                    selectedWordGroup = vocabulary.wordGroup,
                    gender = vocabulary.gender,
                    wordWithAnnotation = vocabulary.word,
                    translation = vocabulary.translation,
                    ending = vocabulary.ending,
                    notes = vocabulary.notes,
                    isIrregularPronunciation = vocabulary.irregularPronunciation?.isNotBlank()
                        ?: false,
                    irregularPronunciation = vocabulary.irregularPronunciation,
                    isFavorite = vocabulary.isFavorite,
                    editingExistingVocabulary = vocabulary.word,
                )
            } ?: UiState()
        }
    }
}
