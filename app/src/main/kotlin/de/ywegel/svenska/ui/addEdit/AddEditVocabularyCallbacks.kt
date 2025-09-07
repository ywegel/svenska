package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup

/**
 * A callback interface to shorten the AddEditScreen parameter list
 */
@Suppress("TooManyFunctions")
interface AddEditVocabularyCallbacks {
    fun updateSelectedWordGroup(group: ViewWordGroup)
    fun updateSelectedSubWordGroup(subGroup: ViewWordSubGroup)
    fun updateGender(gender: Gender?)
    fun updateWordWithAnnotation(word: String)
    fun updateTranslation(translation: String)
    fun updateEnding(ending: String)
    fun updateNotes(notes: String)
    fun updateIsFavorite(isFavorite: Boolean)
    fun updateIsIrregularPronunciation(isIrregular: Boolean)
    fun updateIrregularPronunciation(pronunciation: String)
    fun deleteVocabulary()
    fun saveAndNavigateUp()
    fun hideAnnotationInfo()
}
