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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.quiz.QuizRenderer
import de.ywegel.svenska.ui.quiz.controller.TranslateWithoutEndingsActions
import de.ywegel.svenska.ui.quiz.controller.TranslateWithoutEndingsState
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

class TranslateWithoutEndingsRenderer : QuizRenderer<
    UserAnswer.TranslateWithoutEndingsAnswer,
    TranslateWithoutEndingsState,
    TranslateWithoutEndingsActions,
    Boolean,
    > {

    @Composable
    override fun Prompt(question: QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>) {
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
        question: QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>,
        state: TranslateWithoutEndingsState,
        actions: TranslateWithoutEndingsActions,
    ) {
        TextField(
            value = state.translationInput,
            onValueChange = actions.onInputChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your translation") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done,
            ),
        )
    }

    @Composable
    override fun Solution(
        question: QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>,
        userAnswer: UserAnswer.TranslateWithoutEndingsAnswer,
        userAnswerCorrect: Boolean,
    ) {
        val haptic = LocalHapticFeedback.current

        LaunchedEffect(question.vocabularyId, userAnswer.answer) {
            if (userAnswerCorrect) {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
            }
        }

        val resultMessage = remember(userAnswerCorrect) {
            if (userAnswerCorrect) {
                R.string.quiz_result_correct
            } else {
                R.string.quiz_result_wrong
            }
        }

        val cardColors = if (userAnswerCorrect) {
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
                    text = stringResource(resultMessage),
                    style = SvenskaTheme.typography.titleMedium,
                )
                VerticalSpacerXXS()
                Text(
                    text = question.expectedAnswer.answer,
                    style = SvenskaTheme.typography.bodyLarge,
                    color = SvenskaTheme.colors.primary,
                )
                VerticalSpacerXXS()
                Text(
                    text = stringResource(R.string.quiz_result_wrong_user_answer),
                    style = SvenskaTheme.typography.titleMedium,
                )
                VerticalSpacerXXS()
                Text(
                    text = userAnswer.answer,
                    style = SvenskaTheme.typography.bodyLarge,
                )
            }
        }
    }
}
