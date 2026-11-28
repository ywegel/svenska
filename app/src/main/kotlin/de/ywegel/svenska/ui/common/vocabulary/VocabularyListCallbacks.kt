package de.ywegel.svenska.ui.common.vocabulary

import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.quiz.VocabularyPoolState

interface VocabularyListCallbacks {
    fun onVocabularyClick(vocabulary: Vocabulary, showContainerInformation: Boolean)
    fun onDismissVocabularyDetail()
    fun toggleFavorite(vocabularyId: Int, isFavorite: Boolean)
    fun togglePool(vocabulary: Vocabulary, poolState: VocabularyPoolState)
}
