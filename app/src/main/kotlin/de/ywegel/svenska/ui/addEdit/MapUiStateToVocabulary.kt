@file:Suppress("MatchingDeclarationName")

package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.words.formCount
import de.ywegel.svenska.domain.words.splitEndings
import de.ywegel.svenska.domain.words.withForms
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup

/** The outcome of mapping the input to a [Vocabulary]. An invalid input says what is wrong with it. */
sealed interface SaveResult {
    data class Valid(val vocabulary: Vocabulary) : SaveResult

    /** No [ViewWordGroup] is selected, or the selected one lacks its [ViewWordSubGroup]. */
    data object MissingWordGroup : SaveResult

    /** The highlight stars are unbalanced or outside the changing word. The word field shows this error. */
    data object InvalidHighlights : SaveResult

    /** The written out forms can't be saved, see [WrittenFormsError]. The forms show this error. */
    data class InvalidWrittenForms(val error: WrittenFormsError) : SaveResult
}

/**
 * Maps the input to the [Vocabulary] to save, or returns why it can't be saved.
 * When editing, the [initialVocabulary] keeps its id and `created` timestamp and [containerId] is ignored.
 */
fun mapUiStateToVocabulary(snapshot: AddEditUiState, initialVocabulary: Vocabulary?, containerId: Int): SaveResult {
    val wordGroup = snapshot.selectedWordGroup?.toWordGroup(snapshot.selectedSubGroup)
        ?: return SaveResult.MissingWordGroup
    if (snapshot.wordInput.hasHighlightError) return SaveResult.InvalidHighlights
    snapshot.writtenFormsError?.let { return SaveResult.InvalidWrittenForms(it) }

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
        notes = notes,
        irregularPronunciation = irregularPronunciation?.takeIf { it.isNotBlank() },
        isFavorite = isFavorite,
        lastEdited = System.currentTimeMillis(),
    ).withForms(forms, needsReview = wordGroup != WordGroup.Other && showReviewHint)
}

/**
 * Written out forms are saved as full words, so the endings always build the same forms again.
 * [WrittenFormsError] keeps gaps and spaces out, which would shift the forms when the ending is split again.
 * Endings beyond the forms of the word group are dropped, like the forms themselves.
 */
private fun AddEditUiState.endingFor(wordGroup: WordGroup): String = when {
    wordGroup == WordGroup.Other -> ""
    formsMode == FormsMode.WrittenOut -> forms.map { it.trim() }.filter { it.isNotEmpty() }.joinToString(" ")
    else -> splitEndings(endings).take(wordGroup.formCount).joinToString(" ")
}
