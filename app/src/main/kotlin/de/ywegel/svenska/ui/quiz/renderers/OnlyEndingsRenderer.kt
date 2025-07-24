package de.ywegel.svenska.ui.quiz.renderers

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.quiz.QuizRenderer
import de.ywegel.svenska.ui.quiz.controller.OnlyEndingsActions
import de.ywegel.svenska.ui.quiz.controller.OnlyEndingsState
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

class OnlyEndingsRenderer : QuizRenderer<
    UserAnswer.OnlyEndingsAnswer,
    OnlyEndingsState,
    OnlyEndingsActions,
    Boolean,
    > {

    @Composable
    override fun Prompt(question: QuizQuestion<UserAnswer.OnlyEndingsAnswer>) {
        Text(
            text = question.prompt,
            style = SvenskaTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        VerticalSpacerXXS()
    }

    @Composable
    override fun UserInput(
        question: QuizQuestion<UserAnswer.OnlyEndingsAnswer>,
        state: OnlyEndingsState,
        actions: OnlyEndingsActions,
    ) {
        TextField(
            value = state.endingsInput,
            onValueChange = actions.onEndingsChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your endings") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
            ),
        )
    }

    @Composable
    override fun Solution(
        question: QuizQuestion<UserAnswer.OnlyEndingsAnswer>,
        userAnswer: UserAnswer.OnlyEndingsAnswer,
        userAnswerResult: Boolean,
        wordGroupSection: @Composable (() -> Unit)?,
    ) {
        val cardColors = if (userAnswerResult) {
            CardDefaults.cardColors()
        } else {
            CardDefaults.cardColors(containerColor = SvenskaTheme.colors.errorContainer)
        }

        Card(colors = cardColors) {
            Column(
                Modifier
                    .padding(vertical = Spacings.l, horizontal = Spacings.m)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = stringResource(
                        if (userAnswerResult) {
                            R.string.quiz_result_correct
                        } else {
                            R.string.quiz_result_wrong
                        },
                    ),
                    style = SvenskaTheme.typography.titleMedium,
                )
                wordGroupSection?.let {
                    VerticalSpacerXXS()
                    it()
                }
                VerticalSpacerXXS()
                Text(
                    text = stringResource(R.string.quiz_result_wrong_user_answer),
                    style = SvenskaTheme.typography.titleMedium,
                )
                VerticalSpacerXXS()
                Text(
                    text = userAnswer.endings,
                    style = SvenskaTheme.typography.bodyLarge,
                )
            }
        }
    }
}
