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
import de.ywegel.svenska.di.ImmediateDispatcher
import de.ywegel.svenska.di.IoDispatcher
import de.ywegel.svenska.domain.words.FormBuilder
import de.ywegel.svenska.domain.words.WordGroupMatcher
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

@Suppress("TooManyFunctions")
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
                it.copy(selectedWordGroup = group, selectedSubGroup = ViewWordSubGroup.None).withArticleGender()
            } else {
                it
            }
        }
    }

    override fun updateSelectedSubWordGroup(subGroup: ViewWordSubGroup) {
        _uiState.update { it.copy(selectedSubGroup = subGroup) }
    }

    override fun updateGender(gender: Gender?) {
        _uiState.update { it.copy(gender = gender) }
    }

    override fun updateWordWithAnnotation(word: String) {
        _uiState.update { it.copy(wordWithAnnotation = word).withArticleGender() }
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
        _uiState.update { state ->
            state.copy(writtenForms = state.writtenForms.mapIndexed { i, old -> if (i == index) form else old })
        }
    }

    override fun switchToWrittenOutForms() {
        _uiState.update { it.copy(formsMode = FormsMode.WrittenOut, writtenForms = it.builtFormsForAllSlots()) }
    }

    override fun switchToEndings() {
        _uiState.update {
            if (it.writtenFormsDifferFromEndings) {
                it.copy(showSwitchToEndingsDialog = true)
            } else {
                it.copy(formsMode = FormsMode.Endings)
            }
        }
    }

    override fun confirmSwitchToEndings() {
        _uiState.update { it.copy(formsMode = FormsMode.Endings, showSwitchToEndingsDialog = false) }
    }

    override fun dismissSwitchToEndings() {
        _uiState.update { it.copy(showSwitchToEndingsDialog = false) }
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

    override fun updateIsIrregularPronunciation(isIrregular: Boolean) {
        _uiState.update { it.copy(isIrregularPronunciation = isIrregular) }
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
        // The error is already shown under the word field
        if (uiState.value.wordInput.hasHighlightError) return

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

private fun AddEditUiState.withArticleGender(): AddEditUiState =
    wordInput.articleGender?.let { copy(gender = it) } ?: this

/**
 * @param chosenChangingIndex The changing word the user tapped, null until they do.
 * @param confirmedFormInputs The inputs the user checked the forms for. The review hint stays hidden for them.
 */
data class AddEditUiState(
    val selectedWordGroup: ViewWordGroup? = null,
    val selectedSubGroup: ViewWordSubGroup = ViewWordSubGroup.None,
    val gender: Gender? = null,
    val wordWithAnnotation: String = "",
    val chosenChangingIndex: Int? = null,
    val translation: String = "",
    val endings: String = "",
    val formsMode: FormsMode = FormsMode.Endings,
    val writtenForms: List<String> = emptyList(),
    val confirmedFormInputs: FormInputs? = null,
    val showSwitchToEndingsDialog: Boolean = false,
    val notes: String = "",
    val isIrregularPronunciation: Boolean = false,
    val irregularPronunciation: String? = null,
    val isFavorite: Boolean = false,
    val editingExistingVocabulary: String? = null,
) {
    val wordInput: WordInput by lazy { WordInput.parse(wordWithAnnotation, selectedWordGroup, chosenChangingIndex) }

    /** The word group the forms are built with. Until a subgroup is chosen, it is undefined. */
    val formWordGroup: WordGroup by lazy {
        selectedWordGroup?.toWordGroupOrUndefined(selectedSubGroup) ?: WordGroup.Other
    }

    val formInputs: FormInputs by lazy { FormInputs(wordInput.word, splitEndings(endings), formWordGroup) }

    private val builtForms by lazy { FormBuilder.build(formInputs.word, formInputs.endings, formInputs.wordGroup) }

    /** The forms that are shown and saved: built from the endings, or written out by the user. */
    val forms: List<String>
        get() = if (formsMode == FormsMode.WrittenOut) writtenForms else builtForms.forms

    val hasForms: Boolean
        get() = formWordGroup.formCount > 0

    /** Written out forms are never reviewed, the user typed them in. */
    val showReviewHint: Boolean by lazy {
        formsMode == FormsMode.Endings && builtForms.needsReview && formInputs != confirmedFormInputs
    }

    val suggestedWordGroup: WordGroup? by lazy {
        suggestWordGroup(formInputs, selected = selectedWordGroup?.toWordGroup(selectedSubGroup))
    }

    /** Switching back to the endings would replace forms the user wrote out differently. */
    val writtenFormsDifferFromEndings: Boolean
        get() = writtenForms.map { it.trim() }.filter { it.isNotEmpty() } != builtForms.forms

    /** The built forms as the starting point for writing them out, with an empty field for each missing form. */
    fun builtFormsForAllSlots(): List<String> =
        List(maxOf(formWordGroup.formCount, builtForms.forms.size)) { builtForms.forms.getOrElse(it) { "" } }

    companion object {
        fun fromExistingVocabulary(vocabulary: Vocabulary?): AddEditUiState {
            if (vocabulary == null) return AddEditUiState()

            val wordWithStars = HighlightUtils.reconstructWithStars(vocabulary.word, vocabulary.wordHighlights)
            val state = AddEditUiState(
                selectedWordGroup = ViewWordGroup.fromWordGroup(vocabulary.wordGroup),
                selectedSubGroup = ViewWordSubGroup.fromWordGroup(vocabulary.wordGroup),
                gender = vocabulary.gender,
                wordWithAnnotation = listOf(vocabulary.wordPrefix, wordWithStars, vocabulary.wordSuffix)
                    .filter { it.isNotBlank() }
                    .joinToString(" "),
                chosenChangingIndex = vocabulary.wordPrefix.split(" ").count { it.isNotBlank() },
                translation = vocabulary.translation,
                endings = vocabulary.ending,
                notes = vocabulary.notes,
                isIrregularPronunciation = vocabulary.irregularPronunciation?.isNotBlank() == true,
                irregularPronunciation = vocabulary.irregularPronunciation,
                isFavorite = vocabulary.isFavorite,
                editingExistingVocabulary = vocabulary.word,
            )
            return state.withStoredForms(vocabulary)
        }

        /**
         * Stored forms are shown as they are, never rebuilt. They open as written out forms, if the endings would
         * build something else. Forms that were saved without a pending review count as confirmed.
         */
        private fun AddEditUiState.withStoredForms(vocabulary: Vocabulary): AddEditUiState {
            val storedForms = listOf(vocabulary.form1, vocabulary.form2, vocabulary.form3)
                .dropLastWhile { it == null }
                .map { it.orEmpty() }
            if (storedForms.isEmpty()) return this

            return copy(
                formsMode = if (storedForms == forms) FormsMode.Endings else FormsMode.WrittenOut,
                writtenForms = storedForms,
                confirmedFormInputs = formInputs.takeUnless { vocabulary.formsNeedReview },
            )
        }
    }
}
