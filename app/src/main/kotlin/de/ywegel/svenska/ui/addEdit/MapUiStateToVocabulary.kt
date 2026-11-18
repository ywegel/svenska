package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup

/** The outcome of mapping the input to a [Vocabulary]. An invalid input says what is wrong with it. */
sealed interface SaveResult {
    data class Valid(val vocabulary: Vocabulary) : SaveResult

    /** No [ViewWordGroup] is selected, or the selected one lacks its [ViewWordSubGroup]. */
    data object MissingWordGroup : SaveResult

    /** The highlight stars are unbalanced or outside the changing word. The word field shows this error. */
    data object InvalidHighlights : SaveResult
}

/**
 * Maps a [AddEditUiState] and optional [initialVocabulary] into a [Vocabulary] object.
 *
 * Used in the Add/Edit screen where vocabulary entries are either created from scratch
 * or updated based on user input.
 *
 * It handles:
 * - Converting UI-level representations ([ViewWordGroup], [ViewWordSubGroup]) into domain-level [WordGroup]
 * - Applying default values (e.g., default gender if a noun is selected and no gender was specified)
 * - Splitting the word field into prefix, changing word and suffix, and saving the forms of the changing word
 * - Keeping the original `created` timestamp when editing an existing vocabulary
 *
 * @param snapshot the current UI state containing the user's input
 * @param initialVocabulary the existing vocabulary if the screen is in edit mode, or null for creation mode
 * @param containerId the ID of the container to associate with the new vocabulary (ignored if editing)
 * @return [SaveResult.Valid] with a [Vocabulary] ready for persistence, or the reason the input can't be saved
 */
fun mapUiStateToVocabulary(snapshot: AddEditUiState, initialVocabulary: Vocabulary?, containerId: Int): SaveResult {
    val wordGroup = snapshot.selectedWordGroup?.toWordGroup(snapshot.selectedSubGroup)
        ?: return SaveResult.MissingWordGroup
    if (snapshot.wordInput.hasHighlightError) return SaveResult.InvalidHighlights

    return SaveResult.Valid(snapshot.toVocabulary(wordGroup, initialVocabulary, containerId))
}

/** The vocabulary as the list shows it, also while the input is still incomplete. */
fun AddEditUiState.toPreviewVocabulary(): Vocabulary =
toVocabulary(formWordGroup, initialVocabulary = null, containerId = 0)

private fun AddEditUiState.toVocabulary(
    wordGroup: WordGroup,
    initialVocabulary: Vocabulary?,
    containerId: Int,
): Vocabulary {
    val forms = if (wordGroup == WordGroup.Other) emptyList() else forms.map { it.trim() }
    val base = initialVocabulary ?: Vocabulary(word = "", translation = "", containerId = containerId)

    return base.copy(
        word = wordInput.word,
        wordPrefix = wordInput.prefix,
        wordSuffix = wordInput.suffix,
        wordHighlights = wordInput.wordHighlights,
        translation = translation,
        gender = if (wordGroup is WordGroup.Noun) gender else null,
        wordGroup = wordGroup,
        ending = endingFor(wordGroup),
        form1 = forms.getOrNull(0)?.takeIf { it.isNotEmpty() },
        form2 = forms.getOrNull(1)?.takeIf { it.isNotEmpty() },
        form3 = forms.getOrNull(2)?.takeIf { it.isNotEmpty() },
        formsNeedReview = wordGroup != WordGroup.Other && showReviewHint,
        notes = notes,
        irregularPronunciation = irregularPronunciation.takeIf { it.isNullOrBlank().not() },
        isFavorite = isFavorite,
        lastEdited = System.currentTimeMillis(),
    )
}

/** Written out forms are saved as full words, so the endings always build the same forms again. */
private fun AddEditUiState.endingFor(wordGroup: WordGroup): String = when {
    wordGroup == WordGroup.Other -> ""
    formsMode == FormsMode.WrittenOut -> writtenForms.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")
    else -> splitEndings(endings).joinToString(" ")
}
