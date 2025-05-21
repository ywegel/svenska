@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.bonus.favorites

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.VocabularyContainer
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
) {
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val containerId by viewModel.containerId.collectAsStateWithLifecycle()
    val containerNamesWithIds by viewModel.containerNamesWithIds.collectAsStateWithLifecycle()

    FavoritesAndPronunciationScreen(
        favorites = favorites,
        containerId = containerId,
        containerNamesWithIds = containerNamesWithIds,
        onContainerSelected = viewModel::updateContainerId,
        navigateUp = navigator::navigateUp,
    )
}

@Composable
private fun FavoritesAndPronunciationScreen(
    favorites: List<Vocabulary>,
    containerId: Int?,
    containerNamesWithIds: List<VocabularyContainer>,
    onContainerSelected: (Int?) -> Unit,
    navigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppTextBar(
                title = "Favorites",
                onNavigateUp = navigateUp,
                navigationIcon = SvenskaIcons.Close,
            )
        },
    ) { contentPadding ->
        LazyColumn(Modifier.padding(contentPadding)) {
            item {
                ContainerSelectionMenu(containerId, containerNamesWithIds, onContainerSelected)
            }
            items(favorites) { voc ->
                VocabularyItemCompact(voc)
            }
        }
    }
}

@Composable
private fun ContainerSelectionMenu(
    selectedItem: Int?,
    containerNamesWithIds: List<VocabularyContainer>,
    onContainerSelected: (Int?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedItemPosition by remember { mutableFloatStateOf(0F) }
    val scrollState = rememberScrollState()

    LaunchedEffect(expanded) {
        if (expanded) {
            scrollState.animateScrollTo(selectedItemPosition.toInt())
        }
    }

    Row {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(),
            scrollState = scrollState,
        ) {
            DropdownMenuItem(
                text = { Text("All Containers") },
                onClick = { onContainerSelected(null) },
            )
            containerNamesWithIds.forEach {
                val itemModifier = if (selectedItem == it.id) {
                    Modifier.onGloballyPositioned { position ->
                        selectedItemPosition = position.positionInRoot().y
                    }
                } else {
                    Modifier
                }

                DropdownMenuItem(
                    text = { Text("${it.id}. ${it.name}") },
                    onClick = { onContainerSelected(it.id) },
                    modifier = itemModifier,
                )
            }
        }
    }
}

data class FavoritesAndPronunciationScreenNavArgs(
    val containerId: Int?,
    val screenType: BonusScreen,
)
