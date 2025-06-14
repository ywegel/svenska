package de.ywegel.svenska.ui.quiz.renderers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.quiz.QuizRenderer
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsActions
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsResult
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsState
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

class TranslateWithEndingsRenderer : QuizRenderer<
    UserAnswer.TranslateWithEndingsAnswer,
    TranslateWithEndingsState,
    TranslateWithEndingsActions,
    TranslateWithEndingsResult,
    > {
    @Composable
    override fun Prompt(question: QuizQuestion<UserAnswer.TranslateWithEndingsAnswer>) {
        Text(
            text = question.prompt,
            style = SvenskaTheme.typography.headlineSmall,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }

    @Composable
    override fun UserInput(
        question: QuizQuestion<UserAnswer.TranslateWithEndingsAnswer>,
        state: TranslateWithEndingsState,
        actions: TranslateWithEndingsActions,
    ) {
        val focusManger = LocalFocusManager.current

        TextField(
            value = state.translationInput,
            onValueChange = actions.onTranslationChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Your translation") },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions {
                focusManger.moveFocus(FocusDirection.Down)
            },
        )

        // If no endings are available we don't show the endings text.
        // This could happen, if the current prompt is not swedish or if the vocabulary does not contain endings
        if (!question.expectedAnswer.endings.isNullOrBlank()) {
            TextField(
                value = state.endingInput,
                onValueChange = actions.onEndingChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("translation endings") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions {
                    focusManger.clearFocus()
                },
            )
        }
    }

    @Composable
    override fun Solution(
        question: QuizQuestion<UserAnswer.TranslateWithEndingsAnswer>,
        userAnswer: UserAnswer.TranslateWithEndingsAnswer,
        userAnswerResult: TranslateWithEndingsResult,
    ) {
        val resultMessage = rememberResultTitle(userAnswerResult)

        val cardColors = if (userAnswerResult.translationCorrect && userAnswerResult.endingsCorrect) {
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
                // TODO: Refactor, to respect all when cases above. Right now we don't show the endings
                Text(
                    text = stringResource(resultMessage),
                    style = SvenskaTheme.typography.titleMedium,
                )
                VerticalSpacerXXS()
                FlowRow(verticalArrangement = Arrangement.spacedBy(Spacings.xxxs)) {
                    Text(
                        text = question.expectedAnswer.answer + " ",
                        style = SvenskaTheme.typography.bodyLarge,
                        color = SvenskaTheme.colors.primary,
                    )
                    question.expectedAnswer.endings?.let { endings ->
                        val formattedEndings = buildAnnotatedString {
                            withStyle(SpanStyle(color = SvenskaTheme.colors.onSurface)) {
                                append(stringResource(R.string.quiz_result_endings_formated))
                            }
                            append(" ")
                            withStyle(SpanStyle(color = SvenskaTheme.colors.tertiary)) {
                                append(endings)
                            }
                        }
                        Text(
                            text = formattedEndings,
                            style = SvenskaTheme.typography.bodyLarge,
                        )
                    }
                }
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

@Composable
fun rememberResultTitle(userAnswerResult: TranslateWithEndingsResult) = remember(userAnswerResult) {
    when {
        !userAnswerResult.translationCorrect && !userAnswerResult.endingsCorrect -> {
            R.string.quiz_result_with_endings_wrong_both
        }

        userAnswerResult.translationCorrect && !userAnswerResult.endingsCorrect -> {
            R.string.quiz_result_with_endings_wrong_endings
        }

        !userAnswerResult.translationCorrect && userAnswerResult.endingsCorrect -> {
            R.string.quiz_result_with_endings_wrong_translation
        }

        else -> {
            R.string.quiz_result_correct
        }
    }
}
