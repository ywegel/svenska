@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.quiz.wordGroupQuiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.navigation.SvenskaGraph
import de.ywegel.svenska.ui.common.HorizontalSpacerXS
import de.ywegel.svenska.ui.common.ProgressToolbar
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<SvenskaGraph>(navArgs = WordGroupQuizScreenNavArgs::class)
@Composable
fun WordGroupQuizScreen(navigator: DestinationsNavigator) {
    val viewModel: WordGroupQuizViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    WordGroupQuizScreen(
        uiState = uiState,
        onSubgroupSelected = viewModel::selectSubgroup,
        onCheckClicked = viewModel::check,
        onNextClicked = viewModel::next,
        onNavigateUp = navigator::navigateUp,
    )
}

data class WordGroupQuizScreenNavArgs(
    val containerId: Int?,
)

@Composable
private fun WordGroupQuizScreen(
    uiState: WordGroupQuizUiState,
    onSubgroupSelected: (WordGroup.NounSubgroup) -> Unit,
    onCheckClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    Scaffold(
        topBar = {
            ProgressToolbar(
                progress = uiState.progress,
                progressGoal = uiState.progressGoal,
                fallBackTitle = stringResource(R.string.groupQuiz_fallback_title),
                onNavigateUp = onNavigateUp,
            )
        },
        bottomBar = {
            when (uiState) {
                is WordGroupQuizUiState.QuizItemState -> {
                    CheckButton(
                        uiState = uiState,
                        onCheckClicked = onCheckClicked,
                    )
                }

                else -> {}
            }
        },
    ) { innerPadding ->
        when (uiState) {
            WordGroupQuizUiState.Completed -> CompletedScreen(navigateUp = onNavigateUp)
            WordGroupQuizUiState.Empty -> EmptyContent(navigateUp = onNavigateUp)
            WordGroupQuizUiState.Loading -> LoadingContent()
            is WordGroupQuizUiState.QuizItemState -> QuizContent(
                innerPadding = innerPadding,
                uiState = uiState,
                onSubgroupSelected = onSubgroupSelected,
                onNextClicked = onNextClicked,
            )
        }
    }
}

@Composable
private fun CheckButton(uiState: WordGroupQuizUiState.QuizItemState, onCheckClicked: () -> Unit) {
    Surface(tonalElevation = Spacings.xs) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacings.m)
                .navigationBarsPadding(),
        ) {
            Button(
                onClick = onCheckClicked,
                enabled = uiState.selectedSubgroup != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacings.xl)
                    .height(56.dp),
                shape = SvenskaTheme.shapes.extraLarge,
            ) {
                Text(
                    text = stringResource(R.string.groupQuiz_check),
                    style = SvenskaTheme.typography.titleMedium,
                )
                HorizontalSpacerXS()
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowForward,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyContent(navigateUp: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.groupQuiz_empty_description))
        VerticalSpacerXXS()
        Button(onClick = navigateUp) {
            Text(stringResource(R.string.groupQuiz_navigate_up))
        }
    }
}

@Composable
private fun CompletedScreen(navigateUp: () -> Unit) {
    // TODO: Replace with a real completion screen that shows a statistic
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Quiz finished")
        VerticalSpacerXXS()
        Button(onClick = navigateUp) {
            Text(stringResource(R.string.groupQuiz_navigate_up))
        }
    }
}

@Preview
@Composable
private fun QuizPreview() {
    SvenskaTheme {
        WordGroupQuizScreen(
            onSubgroupSelected = {},
            onCheckClicked = {},
            onNextClicked = {},
            onNavigateUp = {},
            uiState = WordGroupQuizUiState.QuizItemState(
                progress = 4,
                progressGoal = 10,
                vocabulary = vocabulary(),
                correctSubgroup = WordGroup.NounSubgroup.ER,
                selectedSubgroup = WordGroup.NounSubgroup.ER,
            ),
        )
    }
}

@Preview
@Composable
private fun LoadingPreview() {
    SvenskaTheme {
        WordGroupQuizScreen(
            onSubgroupSelected = {},
            onCheckClicked = {},
            onNextClicked = {},
            onNavigateUp = {},
            uiState = WordGroupQuizUiState.Loading,
        )
    }
}

@Preview
@Composable
private fun EmptyPreview() {
    SvenskaTheme {
        WordGroupQuizScreen(
            onSubgroupSelected = {},
            onCheckClicked = {},
            onNextClicked = {},
            onNavigateUp = {},
            uiState = WordGroupQuizUiState.Empty,
        )
    }
}
