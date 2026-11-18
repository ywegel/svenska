package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.words.FormBuilder
import de.ywegel.svenska.domain.words.WordGroupMatcher
import de.ywegel.svenska.domain.words.formCount
import de.ywegel.svenska.domain.words.splitEndings
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import de.ywegel.svenska.ui.common.vocabulary.HighlightUtils

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
    val existingWord: String? = null,
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
        WordGroupMatcher.suggest(formInputs.word, formInputs.endings, formInputs.wordGroup)
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
                existingWord = vocabulary.word,
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
