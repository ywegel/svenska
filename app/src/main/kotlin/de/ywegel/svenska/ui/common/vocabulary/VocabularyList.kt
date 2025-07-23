package de.ywegel.svenska.ui.common.vocabulary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.ui.detail.VocabularyDetailScreen
import de.ywegel.svenska.ui.detail.VocabularyDetailState
import de.ywegel.svenska.ui.overview.VocabularyListItem
import de.ywegel.svenska.ui.theme.Spacings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VocabularyList(
    vocabularies: List<Vocabulary>,
    showContainerInformation: Boolean,
    scrollBehavior: TopAppBarScrollBehavior?,
    contentPadding: PaddingValues,
    navigateToEdit: (vocabulary: Vocabulary) -> Unit,
    navigateToWordGroupScreen: () -> Unit,
    vocabularyListCallbacks: VocabularyListCallbacks,
    vocabularyDetailState: VocabularyDetailState = VocabularyDetailState.Hidden,
    headerItems: (LazyListScope.() -> Unit)? = null,
    footerItems: (LazyListScope.() -> Unit)? = null,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacings.s)
            .let { mod ->
                scrollBehavior?.let { behavior ->
                    mod.nestedScroll(behavior.nestedScrollConnection)
                } ?: mod
            },
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(Spacings.xs),
    ) {
        headerItems?.invoke(this)
        items(
            items = vocabularies,
            key = { it.id },
        ) { vocab ->
            VocabularyListItem(
                vocabulary = vocab,
                modifier = Modifier.animateItem(),
                onClick = {
                    vocabularyListCallbacks.onVocabularyClick(
                        vocabulary = it,
                        showContainerInformation = showContainerInformation,
                    )
                },
            )
        }
        footerItems?.invoke(this)
    }

    // Show detail screen if a vocabulary is selected
    VocabularyDetailScreen(
        state = vocabularyDetailState,
        showContainerInformation = showContainerInformation,
        vocabularyListCallbacks = vocabularyListCallbacks,
        navigateToEdit = navigateToEdit,
        navigateToWordGroupScreen = navigateToWordGroupScreen,
    )
}
