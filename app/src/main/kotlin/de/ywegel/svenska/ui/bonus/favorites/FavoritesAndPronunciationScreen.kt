@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.bonus.favorites

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.vocabularies
import de.ywegel.svenska.navigation.BonusGraph
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerXXXS
import de.ywegel.svenska.ui.container.BonusScreen
import de.ywegel.svenska.ui.overview.VocabularyListItem
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<BonusGraph>(navArgs = FavoritesAndPronunciationScreenNavArgs::class)
@Composable
fun FavoritesAndPronunciationScreen(
    navigator: DestinationsNavigator,
    viewModel: FavoritesAndPronunciationViewModel = hiltViewModel(),
    navArgs: FavoritesAndPronunciationScreenNavArgs,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesAndPronunciationScreen(
        uiState = uiState,
        screenType = navArgs.screenType,
        navigateUp = navigator::navigateUp,
    )
}

@Composable
fun FavoritesAndPronunciationScreen(
    uiState: FavoritesUiState,
    screenType: BonusScreen,
    initialShowExplanation: Boolean = false,
    navigateUp: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showExplanation by rememberSaveable { mutableStateOf(initialShowExplanation) }

    Scaffold(
        topBar = {
            TopAppTextBar(
                title = screenType.userFacingTitle(),
                onNavigateUp = navigateUp,
                navigationIcon = SvenskaIcons.Close,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        icon = SvenskaIcons.Info,
                        contentDescription = stringResource(R.string.accessibility_favorites_pronunciation_explanation),
                    ) {
                        showExplanation = !showExplanation
                    }
                },
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is FavoritesUiState.Loading -> LoadingState(innerPadding)
            is FavoritesUiState.Error -> ErrorState(uiState.message, innerPadding)
            is FavoritesUiState.Success -> SuccessState(
                items = uiState.items,
                screenType = screenType,
                showExplanation = showExplanation,
                padding = innerPadding,
                scrollBehavior = scrollBehavior,
                toggleExplanation = {
                    showExplanation = !showExplanation
                },
            )
        }
    }
}

@Composable
private fun LoadingState(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, padding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = Spacings.xxl),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            style = SvenskaTheme.typography.bodyLarge,
            color = SvenskaTheme.colors.error,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SuccessState(
    items: List<Vocabulary>,
    screenType: BonusScreen,
    showExplanation: Boolean,
    padding: PaddingValues,
    scrollBehavior: TopAppBarScrollBehavior,
    toggleExplanation: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacings.s)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentPadding = padding,
        verticalArrangement = Arrangement.spacedBy(Spacings.xs),
    ) {
        item {
            ExplanationCard(
                showExplanation = showExplanation,
                screenType = screenType,
                onClose = toggleExplanation,
            )
        }
        items(
            items = items,
            key = { it.id },
        ) { voc ->
            VocabularyListItem(voc)
        }
    }
}

data class FavoritesAndPronunciationScreenNavArgs(
    val screenType: BonusScreen,
)

@Composable
private fun ExplanationCard(showExplanation: Boolean, screenType: BonusScreen, onClose: () -> Unit) {
    AnimatedVisibility(
        visible = showExplanation,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .padding(horizontal = Spacings.m, vertical = Spacings.xs)
                    .fillMaxWidth(),
            ) {
                Text(text = screenType.explanationText())
                VerticalSpacerXXXS()
                Button(onClick = onClose) {
                    Text(stringResource(R.string.general_ok))
                }
            }
        }
    }
}

@ReadOnlyComposable
@Composable
fun BonusScreen.explanationText(): String {
    val id = when (this) {
        BonusScreen.Favorites -> R.string.favorites_pronunciation_explanation_favorites
        BonusScreen.SpecialPronunciation -> R.string.favorites_pronunciation_explanation_pronunciation
        else -> null
    }

    return id?.let { stringResource(it) } ?: "This screen type is not supported"
}

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    SvenskaTheme {
        FavoritesAndPronunciationScreen(
            uiState = FavoritesUiState.Error("Something went wrong while loading your favorites. Please try again."),
            screenType = BonusScreen.Favorites,
            navigateUp = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessStatePreview() {
    SvenskaTheme {
        FavoritesAndPronunciationScreen(
            uiState = FavoritesUiState.Success(vocabularies()),
            screenType = BonusScreen.Favorites,
            navigateUp = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessStateWithExplanationPreview() {
    SvenskaTheme {
        FavoritesAndPronunciationScreen(
            uiState = FavoritesUiState.Success(vocabularies()),
            screenType = BonusScreen.Favorites,
            initialShowExplanation = true,
            navigateUp = {},
        )
    }
}
