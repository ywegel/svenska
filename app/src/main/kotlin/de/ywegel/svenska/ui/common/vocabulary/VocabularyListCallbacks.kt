package de.ywegel.svenska.ui.common.vocabulary

import de.ywegel.svenska.data.model.Vocabulary

interface VocabularyListCallbacks {
    fun onVocabularyClick(vocabulary: Vocabulary, showContainerInformation: Boolean)
    fun onDismissVocabularyDetail()
    fun toggleFavorite(vocabularyId: Int, isFavorite: Boolean)
}
