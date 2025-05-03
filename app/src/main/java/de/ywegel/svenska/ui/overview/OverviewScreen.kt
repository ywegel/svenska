@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.vocabularies
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.common.NavigationIconButton
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.destinations.AddEditScreenDestination
import de.ywegel.svenska.ui.destinations.QuizConfigurationScreenDestination
import de.ywegel.svenska.ui.destinations.SearchScreenDestination
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination(navArgsDelegate = OverviewNavArgs::class)
@Composable
fun OverviewScreen(navigator: DestinationsNavigator, navArgs: OverviewNavArgs) {
    val viewModel: OverviewViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OverviewScreen(
        uiState = uiState,
        containerName = navArgs.containerName,
        navigateToAdd = { navigator.navigate(AddEditScreenDestination(viewModel.containerId)) },
        onQuizClick = { navigator.navigate(QuizConfigurationScreenDestination(viewModel.containerId)) },
        navigateToEdit = { item ->
            navigator.navigate(
                AddEditScreenDestination(
                    viewModel.containerId,
                    item,
                ),
            )
        },
        navigateToSearch = { navigator.navigate(SearchScreenDestination(containerId = viewModel.containerId)) },
        navigateUp = navigator::navigateUp,
    )
}

@Composable
private fun OverviewScreen(
    uiState: OverviewUiState,
    containerName: String,
    navigateToAdd: () -> Unit = {},
    onQuizClick: () -> Unit = {},
    navigateToEdit: (vocabulary: Vocabulary) -> Unit = {},
    navigateToSearch: () -> Unit = {},
    navigateUp: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            OverviewTopBar(
                containerName = containerName,
                navigateUp = navigateUp,
                navigateToSearch = navigateToSearch,
            )
        },
        floatingActionButton = { OverviewFab(navigateToAdd, onQuizClick = onQuizClick) },
    ) { contentPadding ->
        if (uiState.showCompactVocabularyItem) {
            LazyColumn(contentPadding = contentPadding) {
                items(uiState.vocabulary, key = { it.id }) { item ->
                    VocabularyItemCompact(item) {
                        navigateToEdit(it)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = Spacings.s),
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(Spacings.xs),
            ) {
                items(
                    items = uiState.vocabulary,
                    key = { it.id },
                ) { vocab ->
                    VocabularyListItem(
                        vocabulary = vocab,
                        onClick = { navigateToEdit(it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun OverviewTopBar(containerName: String, navigateUp: () -> Unit, navigateToSearch: () -> Unit) {
    TopAppBar(
        title = { Text(text = containerName) },
        navigationIcon = {
            NavigationIconButton(
                onNavigateUp = navigateUp,
                navigationIcon = Icons.AutoMirrored.Default.ArrowBack,
            )
        },
        actions = {
            IconButton(
                icon = SvenskaIcons.Search,
                contentDescription = null,
                onClick = navigateToSearch,
            )
        },
    )
}

data class OverviewNavArgs(
    val containerId: Int,
    val containerName: String,
)

@Composable
private fun OverviewFab(onClick: () -> Unit, onQuizClick: () -> Unit) {
    Column {
        FloatingActionButton(onClick = onQuizClick) {
            Icon(SvenskaIcons.Quiz, null)
        }
        VerticalSpacerM()
        FloatingActionButton(onClick = onClick) {
            Icon(SvenskaIcons.Add, null)
        }
    }
}

@Preview
@Composable
private fun OverviewPreview() {
    SvenskaTheme {
        OverviewScreen(OverviewUiState(vocabulary = vocabularies()), containerName = "Test container")
    }
}
