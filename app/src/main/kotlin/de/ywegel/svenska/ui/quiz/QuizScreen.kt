@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.quiz

import androidx.activity.compose.BackHandler
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
import de.ywegel.svenska.ui.quiz.viewmodels.OnlyEndingsQuizViewModel
import de.ywegel.svenska.ui.quiz.viewmodels.TranslateQuizViewModel
import de.ywegel.svenska.ui.quiz.viewmodels.TranslateWithEndingsQuizViewModel
import de.ywegel.svenska.ui.theme.SvenskaIcons

@Destination<QuizGraph>(navArgs = QuizNavArgs::class)
@Composable
fun QuizScreen(navArgs: QuizNavArgs, navigator: DestinationsNavigator) {
    val viewModel = when (val quizMode = navArgs.quizMode) {
        is QuizMode.Translate -> {
            hiltViewModel<TranslateQuizViewModel, TranslateQuizViewModel.Factory>(
                creationCallback = { factory: TranslateQuizViewModel.Factory ->
                    factory.create(quizMode, navArgs.containerId)
                },
            )
        }

        is QuizMode.TranslateWithEndings -> {
            hiltViewModel<TranslateWithEndingsQuizViewModel, TranslateWithEndingsQuizViewModel.Factory>(
                creationCallback = { factory: TranslateWithEndingsQuizViewModel.Factory ->
                    factory.create(quizMode, navArgs.containerId)
                },
            )
        }

        is QuizMode.OnlyEndings -> {
            hiltViewModel<OnlyEndingsQuizViewModel, OnlyEndingsQuizViewModel.Factory>(
                creationCallback = { factory: OnlyEndingsQuizViewModel.Factory ->
                    factory.create(quizMode, navArgs.containerId)
                },
            )
        }
    }

    QuizScreen(viewModel, navigator)
}

@Composable
private fun <A : UserAnswer, S : QuizInputState<A>, AC : Any, AR : Any> QuizScreen(
    viewModel: BaseQuizViewModel<A, S, AC, AR>,
    navigator: DestinationsNavigator,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val inputState by viewModel.inputState.collectAsStateWithLifecycle()

    val navigateUp: () -> Unit = {
        navigator.popBackStack(
            route = QuizConfigurationScreenDestination,
            inclusive = true,
        )
    }

    BackHandler {
        navigateUp()
    }

    QuizScreen(
        uiState = uiState,
        renderer = viewModel.renderer,
        state = inputState,
        actions = viewModel.actions,
        callbacks = viewModel,
        navigateToWordGroupsScreen = { navigator.navigate(WordGroupsScreenDestination) },
        navigateToOverview = navigateUp,
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
    callbacks: QuizCallbacks<A>,
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
                callbacks = callbacks,
                navigateToWordGroupsScreen = navigateToWordGroupsScreen,
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
                    callbacks = callbacks,
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
private fun <A : UserAnswer> QuizToolbar(
    uiState: QuizUiState<*, *>,
    isFavorite: Boolean?,
    navigateUp: () -> Unit,
    callbacks: QuizCallbacks<A>,
    navigateToWordGroupsScreen: () -> Unit,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.quiz_title)) },
        navigationIcon = { NavigationIconButton(onNavigateUp = navigateUp) },
        actions = {
            // TODO: Rethink the "undo" action. Maybe it makes more sense, to have the option to see the quiz history instead. This would break the favorites and the stats
            /* IconButton(
                icon = Icons.AutoMirrored.Outlined.Undo,
                contentDescription = null,
                onClick = callbacks::returnToPreviousQuestion,
                enabled = uiState is QuizUiState.Active && uiState.canReturnToPreviousQuestion,
            ) */
            IconButton(
                icon = SvenskaIcons.Info,
                contentDescription = stringResource(R.string.accessibility_quiz_toolbar_actions_word_group_info),
                onClick = navigateToWordGroupsScreen,
            )
            IconButton(
                icon = if (isFavorite == true) SvenskaIcons.Favorite else SvenskaIcons.FavoriteBorder,
                contentDescription = stringResource(R.string.accessibility_quiz_toolbar_actions_favorite),
                onClick = { isFavorite?.let { callbacks.toggleFavorite(!isFavorite) } },
                enabled = uiState is QuizUiState.Active && isFavorite != null,
            )
        },
    )
}

data class QuizNavArgs(
    val quizMode: QuizMode,
    val containerId: Int?,
)
