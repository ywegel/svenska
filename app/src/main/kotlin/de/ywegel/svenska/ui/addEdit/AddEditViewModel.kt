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

    override fun updateGender(gender: Gender) {
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

private fun AddEditUiState.withArticleGender(): AddEditUiState =
    wordInput.articleGender?.let { copy(gender = it) } ?: this

/**
 * @param chosenChangingIndex The changing word the user tapped, null until they do.
 * @param confirmedFormInputs The inputs the user checked the forms for. The review hint stays hidden for them.
 */
data class AddEditUiState(
    val selectedWordGroup: ViewWordGroup? = null,
    val selectedSubGroup: ViewWordSubGroup = ViewWordSubGroup.None,
    val gender: Gender = Gender.defaultIfEmpty,
    val wordWithAnnotation: String = "",
    val chosenChangingIndex: Int? = null,
    val translation: String = "",
    val endings: String = "",
    val formsMode: FormsMode = FormsMode.Endings,
    val writtenForms: List<String> = emptyList(),
    val confirmedFormInputs: FormInputs? = null,
    val notes: String = "",
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

    /**
     * The forms that are shown and saved: built from the endings, or written out by the user. Only as many as the word
     * group has. Forms beyond that stay in [writtenForms], so they come back when switching to a group with more forms.
     */
    val forms: List<String>
        get() = (if (formsMode == FormsMode.WrittenOut) writtenForms.withAllSlots() else builtForms.forms)
            .take(formWordGroup.formCount)

    val hasForms: Boolean
        get() = formWordGroup.formCount > 0

    /** Written out forms are never reviewed, the user typed them in. */
    val showReviewHint: Boolean by lazy {
        formsMode == FormsMode.Endings && builtForms.needsReview && formInputs != confirmedFormInputs
    }

    val suggestedWordGroup: WordGroup? by lazy {
        suggestWordGroup(formInputs, selected = selectedWordGroup?.toWordGroup(selectedSubGroup))
    }

    /** Only written out forms are checked. Other has no forms, built forms never have gaps or spaces. */
    val writtenFormsError: WrittenFormsError? by lazy {
        if (formsMode != FormsMode.WrittenOut || formWordGroup == WordGroup.Other) return@lazy null
        val forms = forms.map { it.trim() }
        when {
            forms.any { form -> form.any(Char::isWhitespace) } -> WrittenFormsError.Space
            // Empty forms at the end are fine, only an empty one before a filled one is a gap
            forms.dropLastWhile { it.isEmpty() }.any { it.isEmpty() } -> WrittenFormsError.Gap
            else -> null
        }
    }

    /** Switching back to the endings would replace forms the user wrote out differently. */
    val writtenFormsDifferFromEndings: Boolean
        get() = writtenForms.take(formWordGroup.formCount).map { it.trim() }.filter { it.isNotEmpty() } !=
            builtForms.forms.take(formWordGroup.formCount)

    /** The built forms as the starting point for writing them out. */
    fun builtFormsForAllSlots(): List<String> = builtForms.forms.withAllSlots()

    fun withWrittenForm(index: Int, form: String): AddEditUiState =
        copy(writtenForms = writtenForms.withAllSlots().mapIndexed { i, old -> if (i == index) form else old })

    /** The word group decides how many forms there are, so every missing form gets an empty field. */
    private fun List<String>.withAllSlots(): List<String> =
        this + List(maxOf(0, formWordGroup.formCount - size)) { "" }

    companion object {
        fun fromExistingVocabulary(vocabulary: Vocabulary?): AddEditUiState {
            if (vocabulary == null) return AddEditUiState()

            val wordWithStars = HighlightUtils.reconstructWithStars(vocabulary.word, vocabulary.wordHighlights)
            val state = AddEditUiState(
                selectedWordGroup = ViewWordGroup.fromWordGroup(vocabulary.wordGroup),
                selectedSubGroup = ViewWordSubGroup.fromWordGroup(vocabulary.wordGroup),
                gender = vocabulary.gender ?: Gender.defaultIfEmpty,
                wordWithAnnotation = listOf(vocabulary.wordPrefix, wordWithStars, vocabulary.wordSuffix)
                    .filter { it.isNotBlank() }
                    .joinToString(" "),
                chosenChangingIndex = vocabulary.wordPrefix.split(" ").count { it.isNotBlank() },
                translation = vocabulary.translation,
                endings = vocabulary.ending,
                notes = vocabulary.notes,
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
