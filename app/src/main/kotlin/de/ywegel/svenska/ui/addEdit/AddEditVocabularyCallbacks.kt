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
    fun updateGender(gender: Gender)
    fun updateWordWithAnnotation(word: String)
    fun selectChangingWord(index: Int)
    fun updateTranslation(translation: String)
    fun updateEndings(endings: String)
    fun updateWrittenForm(index: Int, form: String)
    fun switchToWrittenOutForms()
    fun switchToEndings()
    fun confirmForms()
    fun applySuggestedWordGroup()
    fun updateNotes(notes: String)
    fun updateIsFavorite(isFavorite: Boolean)
    fun updateIrregularPronunciation(pronunciation: String)
    fun deleteVocabulary()
    fun saveAndNavigateUp()
}
