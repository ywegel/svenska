@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.bonus.favorites

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.navigation.BonusGraph
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.container.BonusScreen
import de.ywegel.svenska.ui.overview.VocabularyItemCompact
import de.ywegel.svenska.ui.theme.SvenskaIcons

@Destination<BonusGraph>(navArgs = FavoritesAndPronunciationScreenNavArgs::class)
@Composable
fun FavoritesAndPronunciationScreen(
    navigator: DestinationsNavigator,
    viewModel: FavoritesAndPronunciationViewModel = hiltViewModel(),
    navArgs: FavoritesAndPronunciationScreenNavArgs,
) {
    val favorites by viewModel.bonusItems.collectAsStateWithLifecycle()

    FavoritesAndPronunciationScreen(
        favorites = favorites,
        screenType = navArgs.screenType,
        navigateUp = navigator::navigateUp,
    )
}

@Composable
private fun FavoritesAndPronunciationScreen(
    favorites: List<Vocabulary>,
    screenType: BonusScreen,
    navigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppTextBar(
                title = screenType.userFacingTitle(),
                onNavigateUp = navigateUp,
                navigationIcon = SvenskaIcons.Close,
            )
        },
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            items(favorites) { voc ->
                VocabularyItemCompact(voc)
            }
        }
    }
}

data class FavoritesAndPronunciationScreenNavArgs(
    val screenType: BonusScreen,
)
