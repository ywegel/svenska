@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.quiz

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.QuizConfigurationScreenDestination
import com.ramcosta.composedestinations.generated.destinations.WordGroupsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.quiz.model.QuizMode
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.navigation.QuizGraph
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.common.NavigationIconButton
import de.ywegel.svenska.ui.quiz.viewmodels.TranslateQuizViewModel
import de.ywegel.svenska.ui.quiz.viewmodels.TranslateWithEndingsQuizViewModel
import de.ywegel.svenska.ui.theme.SvenskaIcons

@Destination<QuizGraph>(navArgs = QuizNavArgs::class)
@Composable
fun QuizScreen(navArgs: QuizNavArgs, navigator: DestinationsNavigator) {
    when (val quizMode = navArgs.quizMode) {
        is QuizMode.Translate -> {
            val viewModel: TranslateQuizViewModel = hiltViewModel(
                creationCallback = { factory: TranslateQuizViewModel.Factory ->
                    factory.create(quizMode.mode, navArgs.containerId)
                },
            )
            QuizScreen(viewModel, navigator)
        }

        is QuizMode.TranslateWithEndings -> {
            val viewModel: TranslateWithEndingsQuizViewModel = hiltViewModel(
                creationCallback = { factory: TranslateWithEndingsQuizViewModel.Factory ->
                    factory.create(quizMode.mode, navArgs.containerId)
                },
            )
            QuizScreen(viewModel, navigator)
        }

        is QuizMode.OnlyEndings -> {
            TODO("Implement OnlyEndings quiz type")
        }
    }
}

@Composable
private fun <A : UserAnswer, S : QuizInputState<A>, AC : Any, AR : Any> QuizScreen(
    viewModel: BaseQuizViewModel<A, S, AC, AR>,
    navigator: DestinationsNavigator,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inputState by viewModel.inputState.collectAsStateWithLifecycle()

    QuizScreen(
        uiState = uiState,
        renderer = viewModel.renderer,
        state = inputState,
        actions = viewModel.actions,
        returnToPreviousQuestion = viewModel::returnToPreviousQuestion,
        toggleFavorite = viewModel::toggleFavorite,
        checkAnswer = viewModel::checkAnswer,
        nextWord = viewModel::nextWord,
        navigateToWordGroupsScreen = { navigator.navigate(WordGroupsScreenDestination) },
        navigateToOverview = {
            navigator.popBackStack(
                route = QuizConfigurationScreenDestination,
                inclusive = true,
            )
        },
        onStartNewQuiz = {
            navigator.popBackStack(QuizConfigurationScreenDestination, inclusive = false)
        },
    )
}

@Composable
private fun <A : UserAnswer, State : QuizInputState<A>, Actions : Any, AnswerResult : Any> QuizScreen(
    uiState: QuizUiState<A, AnswerResult>,
    renderer: QuizRenderer<A, State, Actions, AnswerResult>,
    state: State,
    actions: Actions,
    checkAnswer: (A) -> Unit,
    nextWord: () -> Unit,
    returnToPreviousQuestion: () -> Unit,
    toggleFavorite: (Boolean) -> Unit,
    navigateToWordGroupsScreen: () -> Unit,
    navigateToOverview: () -> Unit,
    onStartNewQuiz: () -> Unit,
) {
    Scaffold(
        topBar = {
            QuizToolbar(
                uiState = uiState,
                navigateUp = navigateToOverview,
                isFavorite = (uiState as? QuizUiState.Active<*, *>)?.vocabularyIsFavorite,
                toggleFavorite = toggleFavorite,
                navigateToWordGroupsScreen = navigateToWordGroupsScreen,
                returnToLastWord = returnToPreviousQuestion,
            )
        },
    ) { innerPadding ->
        when (uiState) {
            is QuizUiState.Loading -> {
                QuizLoadingScreen(innerPadding)
            }

            is QuizUiState.Error -> {
                QuizErrorScreen(
                    innerPadding = innerPadding,
                    exception = uiState.exception,
                    onRetry = uiState.retryAction,
                    onBack = navigateToOverview,
                )
            }

            is QuizUiState.Active -> {
                QuizContent(
                    innerPadding = innerPadding,
                    renderer = renderer,
                    currentQuestion = uiState.quizQuestion,
                    state = state,
                    actions = actions,
                    userAnswer = uiState.userAnswer,
                    userAnswerCorrect = uiState.userAnswerResult,
                    checkAnswer = checkAnswer,
                    nextWord = nextWord,
                )
            }

            // TODO: Remove toolbar from quiz finished screen
            is QuizUiState.Finished -> {
                QuizFinishedScreen(
                    innerPadding = innerPadding,
                    correctAnswers = uiState.correctAnswers,
                    totalQuestions = uiState.totalQuestions,
                    score = uiState.score,
                    onStartNewQuiz = onStartNewQuiz,
                    navigateUp = navigateToOverview,
                )
            }
        }
    }
}

@Composable
private fun QuizToolbar(
    uiState: QuizUiState<*, *>,
    isFavorite: Boolean?,
    navigateUp: () -> Unit,
    toggleFavorite: (Boolean) -> Unit,
    navigateToWordGroupsScreen: () -> Unit,
    returnToLastWord: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.quiz_title)) },
        navigationIcon = { NavigationIconButton(onNavigateUp = navigateUp) },
        actions = {
            IconButton(
                icon = Icons.AutoMirrored.Outlined.Undo,
                contentDescription = null,
                onClick = returnToLastWord,
                enabled = uiState is QuizUiState.Active && uiState.canReturnToPreviousQuestion,
            )
            IconButton(
                icon = SvenskaIcons.Info,
                contentDescription = stringResource(R.string.accessibility_quiz_toolbar_actions_word_group_info),
                onClick = navigateToWordGroupsScreen,
            )
            IconButton(
                icon = if (isFavorite == true) SvenskaIcons.Favorite else SvenskaIcons.FavoriteBorder,
                contentDescription = stringResource(R.string.accessibility_quiz_toolbar_actions_favorite),
                onClick = { isFavorite?.let { toggleFavorite(!isFavorite) } },
                enabled = uiState is QuizUiState.Active && isFavorite != null,
            )
        },
    )
}

data class QuizNavArgs(
    val quizMode: QuizMode,
    val containerId: Int?,
)
