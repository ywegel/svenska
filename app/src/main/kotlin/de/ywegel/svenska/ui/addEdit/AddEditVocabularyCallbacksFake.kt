package de.ywegel.svenska.ui.addEdit

import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import org.jetbrains.annotations.VisibleForTesting

@VisibleForTesting
object AddEditVocabularyCallbacksFake : AddEditVocabularyCallbacks {
    override fun updateSelectedWordGroup(group: ViewWordGroup) {}
    override fun updateSelectedSubWordGroup(subGroup: ViewWordSubGroup) {}
    override fun updateGender(gender: Gender?) {}
    override fun updateWordWithAnnotation(word: String) {}
    override fun updateTranslation(translation: String) {}
    override fun updateEnding(ending: String) {}
    override fun updateNotes(notes: String) {}
    override fun updateIsFavorite(isFavorite: Boolean) {}
    override fun updateIsIrregularPronunciation(isIrregular: Boolean) {}
    override fun updateIrregularPronunciation(pronunciation: String) {}
    override fun deleteVocabulary() {}
    override fun saveAndNavigateUp() {}
    override fun hideAnnotationInfo() {}
}
