@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import de.ywegel.svenska.data.model.Vocabulary

@Composable
fun VocabularyDetailScreen(
    state: VocabularyDetailState,
    onDismiss: () -> Unit = {},
    onEditClick: (Vocabulary) -> Unit = {},
    navigateToWordGroupScreen: () -> Unit,
) {
    if (state is VocabularyDetailState.Visible) {
        val viewModel: VocabularyDetailViewModel = hiltViewModel()

        var isFavorite by remember { mutableStateOf(state.selectedVocabulary.isFavorite) }

        VocabularyDetailContent(
            vocabulary = state.selectedVocabulary,
            isFavorite = isFavorite,
            onDismiss = onDismiss,
            onEditClick = onEditClick,
            onFavoriteChange = { newValue ->
                isFavorite = newValue
                viewModel.toggleFavorite(state.selectedVocabulary.id, newValue)
            },
            navigateToWordGroupScreen = navigateToWordGroupScreen,
        )
    }
}

sealed interface VocabularyDetailState {
    data object Hidden : VocabularyDetailState
    data class Visible(val selectedVocabulary: Vocabulary) : VocabularyDetailState
}
