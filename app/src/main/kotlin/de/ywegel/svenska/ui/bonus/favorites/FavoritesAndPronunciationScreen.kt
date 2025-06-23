@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.bonus.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.vocabularies
import de.ywegel.svenska.navigation.BonusGraph
import de.ywegel.svenska.ui.common.TopAppTextBar
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
fun FavoritesAndPronunciationScreen(uiState: FavoritesUiState, screenType: BonusScreen, navigateUp: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppTextBar(
                title = screenType.userFacingTitle(),
                onNavigateUp = navigateUp,
                navigationIcon = SvenskaIcons.Close,
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is FavoritesUiState.Loading -> LoadingState(innerPadding)
            is FavoritesUiState.Error -> ErrorState(uiState.message, innerPadding)
            is FavoritesUiState.Success -> SuccessState(uiState.items, innerPadding)
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
private fun SuccessState(items: List<Vocabulary>, padding: PaddingValues) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = Spacings.s),
        contentPadding = padding,
        verticalArrangement = Arrangement.spacedBy(Spacings.xs),
    ) {
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

@Preview(showBackground = true)
@Composable
private fun ErrorStatePreview() {
    SvenskaTheme {
        ErrorState(
            message = "Something went wrong while loading your favorites. Please try again later.",
            padding = PaddingValues(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SuccessStatePreview() {
    SvenskaTheme {
        SuccessState(
            items = vocabularies(),
            padding = PaddingValues(),
        )
    }
}
