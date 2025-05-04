package de.ywegel.svenska.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun <A : UserAnswer, State : QuizInputState<A>, Actions : Any, AnswerResult : Any> QuizContent(
    innerPadding: PaddingValues,
    renderer: QuizRenderer<A, State, Actions, AnswerResult>,
    currentQuestion: QuizQuestion<A>,
    state: State,
    actions: Actions,
    userAnswer: A?,
    userAnswerCorrect: AnswerResult?,
    checkAnswer: (A) -> Unit,
    nextWord: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(Spacings.m),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.quiz_translate),
            style = SvenskaTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        VerticalSpacerXXS()

        renderer.Prompt(currentQuestion)

        VerticalSpacerM()

        renderer.UserInput(currentQuestion, state, actions)

        VerticalSpacerM()

        if (userAnswer != null && userAnswerCorrect != null) {
            renderer.Solution(currentQuestion, userAnswer, userAnswerCorrect)

            VerticalSpacerM()
        }

        Button(onClick = {
            checkAnswer(state.toUserAnswer())
            focusManager.clearFocus()
        }) {
            Text(stringResource(R.string.quiz_check))
        }

        VerticalSpacerXXS()

        Button(onClick = {
            nextWord()
        }) {
            Text(stringResource(R.string.quiz_next))
        }
    }
}
