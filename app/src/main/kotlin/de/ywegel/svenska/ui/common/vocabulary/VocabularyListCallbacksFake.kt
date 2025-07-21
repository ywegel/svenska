package de.ywegel.svenska.ui.common.vocabulary

import androidx.annotation.VisibleForTesting
import de.ywegel.svenska.data.model.Vocabulary

@VisibleForTesting
object VocabularyListCallbacksFake : VocabularyListCallbacks {
    override fun onVocabularyClick(vocabulary: Vocabulary, showContainerInformation: Boolean) {
        /* Nothing to do here */
    }

    override fun onDismissVocabularyDetail() {
        /* Nothing to do here */
    }

    override fun toggleFavorite(vocabularyId: Int, isFavorite: Boolean) {
        /* Nothing to do here */
    }
}
