@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.overview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.AddVocabularyScreenDestination
import com.ramcosta.composedestinations.generated.destinations.EditVocabularyScreenDestination
import com.ramcosta.composedestinations.generated.destinations.QuizConfigurationScreenDestinationNavArgs
import com.ramcosta.composedestinations.generated.destinations.SearchScreenDestination
import com.ramcosta.composedestinations.generated.destinations.WordGroupsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.vocabularies
import de.ywegel.svenska.navigation.SvenskaGraph
import de.ywegel.svenska.ui.common.HorizontalSpacerS
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.vocabulary.VocabularyList
import de.ywegel.svenska.ui.common.vocabulary.VocabularyListCallbacks
import de.ywegel.svenska.ui.common.vocabulary.VocabularyListCallbacksFake
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme
import kotlinx.coroutines.delay

@Destination<SvenskaGraph>(navArgs = OverviewNavArgs::class)
@Composable
fun OverviewScreen(navigator: DestinationsNavigator, navArgs: OverviewNavArgs) {
    val viewModel: OverviewViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OverviewScreen(
        uiState = uiState,
        containerName = navArgs.containerName,
        navigateToAdd = { navigator.navigate(AddVocabularyScreenDestination(viewModel.containerId)) },
        onQuizClick = {
            navigator.navigate(
                NavGraphs.quiz(QuizConfigurationScreenDestinationNavArgs(containerId = viewModel.containerId)),
            )
        },
        navigateToEdit = { item ->
            navigator.navigate(
                EditVocabularyScreenDestination(
                    containerId = viewModel.containerId,
                    initialVocabulary = item,
                ),
            )
        },
        navigateToSearch = { navigator.navigate(SearchScreenDestination(containerId = viewModel.containerId)) },
        navigateUp = navigator::navigateUp,
        vocabularyListCallbacks = viewModel,
        navigateToWordGroupScreen = { navigator.navigate(WordGroupsScreenDestination) },
    )
}

@Composable
private fun OverviewScreen(
    uiState: OverviewUiState,
    containerName: String,
    navigateToAdd: () -> Unit,
    onQuizClick: () -> Unit,
    navigateToSearch: () -> Unit,
    navigateUp: () -> Unit,
    navigateToWordGroupScreen: () -> Unit,
    navigateToEdit: (vocabulary: Vocabulary) -> Unit,
    vocabularyListCallbacks: VocabularyListCallbacks,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppTextBar(
                title = containerName,
                onNavigateUp = navigateUp,
                navigationIcon = Icons.AutoMirrored.Default.ArrowBack,
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(
                        icon = SvenskaIcons.Search,
                        contentDescription = null,
                        onClick = navigateToSearch,
                    )
                },
            )
        },
        floatingActionButton = { OverviewFab(navigateToAdd, onQuizClick = onQuizClick) },
    ) { contentPadding ->
        if (uiState.showCompactVocabularyItem) {
            LazyColumn(
                contentPadding = contentPadding,
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            ) {
                items(uiState.vocabulary, key = { it.id }) { item ->
                    VocabularyItemCompact(item) {
                        vocabularyListCallbacks.onVocabularyClick(vocabulary = it, showContainerInformation = false)
                    }
                }
            }
        } else {
            VocabularyList(
                vocabularies = uiState.vocabulary,
                showContainerInformation = false,
                vocabularyDetailState = uiState.detailViewState,
                scrollBehavior = scrollBehavior,
                contentPadding = contentPadding,
                navigateToEdit = {
                    vocabularyListCallbacks.onDismissVocabularyDetail()
                    navigateToEdit(it)
                },
                navigateToWordGroupScreen = navigateToWordGroupScreen,
                vocabularyListCallbacks = vocabularyListCallbacks,
            )
        }
    }
}

data class OverviewNavArgs(
    val containerId: Int,
    val containerName: String,
)

@Composable
private fun OverviewFab(onAddClick: () -> Unit, onQuizClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.End) {
        AnimatedFabWithText(
            onClick = onQuizClick,
            text = stringResource(R.string.overview_fab_quiz),
            icon = SvenskaIcons.Quiz,
            contentDescription = stringResource(R.string.accessibility_overview_fab_quiz),
        )
        VerticalSpacerM()
        AnimatedFabWithText(
            onClick = onAddClick,
            text = stringResource(R.string.overview_fab_add),
            icon = SvenskaIcons.Add,
            contentDescription = stringResource(R.string.accessibility_overview_fab_add),
        )
    }
}

@Composable
private fun AnimatedFabWithText(onClick: () -> Unit, text: String, icon: ImageVector, contentDescription: String?) {
    var isExpanded by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        // TODO: Maybe, instead of a timer, make the text disappear on scroll
        delay(OVERVIEW_FAB_DISAPPEARING_ANIMATION_DELAY)
        isExpanded = false
    }

    FloatingActionButton(onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = Spacings.m),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(icon, contentDescription)

            AnimatedVisibility(
                visible = isExpanded,
                enter = EnterTransition.None,
                exit = fadeOut(animationSpec = tween(durationMillis = ANIMATION_DURATION_MS)) +
                    shrinkHorizontally(
                        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
                        shrinkTowards = Alignment.End,
                    ),
            ) {
                Row {
                    HorizontalSpacerS()
                    Text(text = text, maxLines = 1)
                }
            }
        }
    }
}

private const val OVERVIEW_FAB_DISAPPEARING_ANIMATION_DELAY = 4000L
private const val ANIMATION_DURATION_MS = 800

@Preview
@Composable
private fun OverviewPreview() {
    SvenskaTheme {
        OverviewScreen(
            uiState = OverviewUiState(vocabulary = vocabularies()),
            containerName = "Test container",
            navigateToAdd = {},
            onQuizClick = {},
            navigateToEdit = {},
            navigateToSearch = {},
            navigateUp = {},
            navigateToWordGroupScreen = {},
            vocabularyListCallbacks = VocabularyListCallbacksFake,
        )
    }
}
