package de.ywegel.svenska.domain.addEdit

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.ui.addEdit.AddEditUiState
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.common.vocabulary.HighlightUtils

/**
 * Maps a [AddEditUiState] and optional [initialVocabulary] into a [Vocabulary] object.
 *
 * This use case is used in Add/Edit screens where vocabulary entries are either created from scratch
 * or updated based on user input.
 *
 * It handles:
 * - Converting UI-level representations ([ViewWordGroup], [de.ywegel.svenska.data.model.ViewWordSubGroup]) into domain-level [de.ywegel.svenska.data.model.WordGroup]
 * - Applying default values (e.g., default gender if a noun is selected and no gender was specified)
 * - Cleaning the annotated input word and extracting character highlights
 * - Keeping the original `created` timestamp when editing an existing vocabulary
 *
 * @param snapshot the current UI state containing the user's input
 * @param initialVocabulary the existing vocabulary if the screen is in edit mode, or null for creation mode
 * @param containerId the ID of the container to associate with the new vocabulary (ignored if editing)
 * @return a [Vocabulary] object ready for persistence, or null if the required word group could not be determined
 *
 * Returns null if:
 * - No [ViewWordGroup] is selected
 * - The selected [ViewWordGroup] does not match the [ViewWordSubGroup] (e.g., noun selected but verb subgroup passed)
 *
 * This allows the ViewModel to signal validation errors and block saving if required fields are missing.
 */

class MapUiStateToVocabularyUseCase {

    operator fun invoke(snapshot: AddEditUiState, initialVocabulary: Vocabulary?, containerId: Int): Vocabulary? {
        val viewWordGroup = snapshot.selectedWordGroup ?: return null
        val wordGroup = viewWordGroup.toWordGroup(snapshot.selectedSubGroup) ?: return null

        val (cleanWord, wordHighlights) = HighlightUtils.parseHighlights(snapshot.wordWithAnnotation)
            .getOrElse { return null }

        val gender = if (viewWordGroup == ViewWordGroup.Noun && snapshot.gender == null) {
            Gender.defaultIfEmpty
        } else {
            snapshot.gender
        }

        val irregularPronunciation = snapshot.irregularPronunciation
            ?.takeIf { snapshot.isIrregularPronunciation }

        return initialVocabulary?.copy(
            word = cleanWord,
            wordHighlights = wordHighlights,
            translation = snapshot.translation,
            gender = gender,
            wordGroup = wordGroup,
            ending = snapshot.ending,
            notes = snapshot.notes,
            irregularPronunciation = irregularPronunciation,
            isFavorite = snapshot.isFavorite,
            lastEdited = System.currentTimeMillis(),
        ) ?: Vocabulary(
            word = cleanWord,
            wordHighlights = wordHighlights,
            translation = snapshot.translation,
            gender = gender,
            wordGroup = wordGroup,
            ending = snapshot.ending,
            notes = snapshot.notes,
            irregularPronunciation = irregularPronunciation,
            isFavorite = snapshot.isFavorite,
            containerId = containerId,
        )
    }
}
