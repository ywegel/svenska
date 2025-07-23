@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.ui.common.vocabulary.VocabularyListCallbacks

/**
 * @param showContainerInformation shows which container the vocabulary is in
 */
@Composable
fun VocabularyDetailScreen(
    state: VocabularyDetailState,
    showContainerInformation: Boolean = false,
    vocabularyListCallbacks: VocabularyListCallbacks,
    navigateToEdit: (vocabulary: Vocabulary) -> Unit,
    navigateToWordGroupScreen: () -> Unit,
) {
    if (state is VocabularyDetailState.Visible) {
        // TODO: Remove this, when refactoring all the logic from the Screens-viewmodel into a VocabularyDetailScreenViewModel. See #45
        var isFavorite by rememberSaveable { mutableStateOf(state.selectedVocabulary.isFavorite) }

        VocabularyDetailContent(
            vocabulary = state.selectedVocabulary,
            container = state.selectedVocabularyContainer,
            isFavorite = isFavorite,
            showContainerInformation = showContainerInformation,
            onDismiss = vocabularyListCallbacks::onDismissVocabularyDetail,
            onEditClick = navigateToEdit,
            onFavoriteChange = { newValue ->
                vocabularyListCallbacks.toggleFavorite(state.selectedVocabulary.id, newValue)
                isFavorite = newValue
            },
            navigateToWordGroupScreen = navigateToWordGroupScreen,
        )
    }
}

sealed interface VocabularyDetailState {
    data object Hidden : VocabularyDetailState
    data class Visible(
        val selectedVocabulary: Vocabulary,
        val selectedVocabularyContainer: VocabularyContainer? = null,
    ) : VocabularyDetailState
}
