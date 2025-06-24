package de.ywegel.svenska.ui.quiz

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.domain.quiz.model.QuizQuestion
import de.ywegel.svenska.domain.quiz.model.QuizQuestionPromptData
import de.ywegel.svenska.domain.quiz.model.UserAnswer
import de.ywegel.svenska.ui.common.HorizontalSpacerXXS
import de.ywegel.svenska.ui.common.HorizontalSpacerXXXS
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.common.VerticalSpacerXXXS
import de.ywegel.svenska.ui.common.vocabulary.abbreviation
import de.ywegel.svenska.ui.common.vocabulary.mainGroupAbbreviation
import de.ywegel.svenska.ui.common.vocabulary.subGroupAbbreviation
import de.ywegel.svenska.ui.common.vocabulary.wordGroupBadge.StaticWordGroupBadgeExtended
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsActions
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsResult
import de.ywegel.svenska.ui.quiz.controller.TranslateWithEndingsState
import de.ywegel.svenska.ui.quiz.controller.TranslateWithoutEndingsActions
import de.ywegel.svenska.ui.quiz.controller.TranslateWithoutEndingsState
import de.ywegel.svenska.ui.quiz.renderers.TranslateWithEndingsRenderer
import de.ywegel.svenska.ui.quiz.renderers.TranslateWithoutEndingsRenderer
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
    callbacks: QuizCallbacks<A>,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .imePadding()
            .verticalScroll(state = rememberScrollState())
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

        VerticalSpacerXXXS()

        renderer.Prompt(currentQuestion)

        VerticalSpacerXXXS()

        WordGroupSection(currentQuestion.promptData)

        VerticalSpacerM()

        renderer.UserInput(currentQuestion, state, actions)

        VerticalSpacerM()

        if (userAnswer != null && userAnswerCorrect != null) {
            renderer.Solution(currentQuestion, userAnswer, userAnswerCorrect)

            VerticalSpacerM()
        }

        Button(
            onClick = {
                callbacks.checkAnswer(state.toUserAnswer())
                focusManager.clearFocus()
            },
        ) {
            Text(stringResource(R.string.quiz_check))
        }

        VerticalSpacerXXS()

        Button(
            onClick = {
                callbacks.nextWord()
            },
        ) {
            Text(stringResource(R.string.quiz_next))
        }
    }
}

@Composable
private fun WordGroupSection(promptData: QuizQuestionPromptData?) {
    AnimatedVisibility(promptData?.wordGroup != null) {
        promptData?.wordGroup?.let { wordGroup ->
            FlowRow(
                itemVerticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(Spacings.xxxs),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StaticWordGroupBadgeExtended(
                        mainWordGroup = wordGroup.mainGroupAbbreviation(),
                        subWordGroup = wordGroup.subGroupAbbreviation(),
                    )
                    if (promptData.gender != null && wordGroup is WordGroup.Noun) {
                        HorizontalSpacerXXXS()
                        Text(
                            text = promptData.gender.abbreviation(),
                            color = SvenskaTheme.colors.primary,
                        )
                    }
                }
                if (!promptData.endings.isNullOrBlank()) {
                    HorizontalSpacerXXS()
                    Text(
                        text = "Endings: ${promptData.endings}",
                        style = SvenskaTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun QuizContentPreview() {
    SvenskaTheme {
        Surface {
            QuizContent(
                innerPadding = PaddingValues(),
                renderer = TranslateWithoutEndingsRenderer(),
                currentQuestion = QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>(
                    vocabularyId = 2,
                    prompt = "testPrompt",
                    expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("testAnswer"),
                    promptData = QuizQuestionPromptData(
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                        endings = "-en -ar -arna",
                    ),
                ),
                state = TranslateWithoutEndingsState("testInput"),
                actions = TranslateWithoutEndingsActions({}),
                userAnswer = null,
                userAnswerCorrect = false,
                callbacks = QuizCallbacksWithoutEndingsFake,
            )
        }
    }
}

@Preview
@Composable
private fun QuizContentNoEndingsPreview() {
    SvenskaTheme {
        Surface {
            QuizContent(
                innerPadding = PaddingValues(),
                renderer = TranslateWithoutEndingsRenderer(),
                currentQuestion = QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>(
                    vocabularyId = 2,
                    prompt = "testPrompt",
                    expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("testAnswer"),
                    promptData = QuizQuestionPromptData(
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                        endings = "",
                    ),
                ),
                state = TranslateWithoutEndingsState("testInput"),
                actions = TranslateWithoutEndingsActions({}),
                userAnswer = null,
                userAnswerCorrect = false,
                callbacks = QuizCallbacksWithoutEndingsFake,
            )
        }
    }
}

@Preview
@Composable
private fun QuizContentWithAnswerPreview() {
    SvenskaTheme {
        Surface {
            QuizContent(
                innerPadding = PaddingValues(),
                renderer = TranslateWithoutEndingsRenderer(),
                currentQuestion = QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>(
                    vocabularyId = 2,
                    prompt = "testPrompt",
                    expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("testAnswer"),
                    promptData = QuizQuestionPromptData(
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                        endings = "-en -ar -arna",
                        gender = Gender.Ultra,
                    ),
                ),
                state = TranslateWithoutEndingsState("testInput"),
                actions = TranslateWithoutEndingsActions({}),
                userAnswer = UserAnswer.TranslateWithoutEndingsAnswer("somethingWrong"),
                userAnswerCorrect = false,
                callbacks = QuizCallbacksWithoutEndingsFake,
            )
        }
    }
}

@Preview
@Composable
private fun QuizContentLongEndingsPreview() {
    SvenskaTheme {
        Surface {
            QuizContent(
                innerPadding = PaddingValues(),
                renderer = TranslateWithoutEndingsRenderer(),
                currentQuestion = QuizQuestion<UserAnswer.TranslateWithoutEndingsAnswer>(
                    vocabularyId = 2,
                    prompt = "TV",
                    expectedAnswer = UserAnswer.TranslateWithoutEndingsAnswer("teve"),
                    promptData = QuizQuestionPromptData(
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                        endings = "-n teveapparater teveapparaterna",
                        gender = Gender.Ultra,
                    ),
                ),
                state = TranslateWithoutEndingsState("testInput"),
                actions = TranslateWithoutEndingsActions({}),
                userAnswer = null,
                userAnswerCorrect = false,
                callbacks = QuizCallbacksWithoutEndingsFake,
            )
        }
    }
}

@VisibleForTesting
private object QuizCallbacksWithoutEndingsFake : QuizCallbacks<UserAnswer.TranslateWithoutEndingsAnswer> {
    override fun checkAnswer(input: UserAnswer.TranslateWithoutEndingsAnswer) {}
    override fun nextWord() {}
    override fun toggleFavorite(isFavorite: Boolean) {}
    override fun returnToPreviousQuestion() {}
}

@Preview
@Composable
private fun QuizContentWithEndingsLongEndingsPreview() {
    SvenskaTheme {
        Surface {
            QuizContent(
                innerPadding = PaddingValues(),
                renderer = TranslateWithEndingsRenderer(),
                currentQuestion = QuizQuestion<UserAnswer.TranslateWithEndingsAnswer>(
                    vocabularyId = 2,
                    prompt = "TV",
                    expectedAnswer = UserAnswer.TranslateWithEndingsAnswer(
                        answer = "teve but extra long",
                        endings = "-n teveapparater teveapparaterna",
                    ),
                    promptData = QuizQuestionPromptData(
                        wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.AR),
                        endings = "-n teveapparater teveapparaterna",
                        gender = Gender.Ultra,
                    ),
                ),
                state = TranslateWithEndingsState("testInput"),
                actions = TranslateWithEndingsActions({}, {}),
                userAnswer = UserAnswer.TranslateWithEndingsAnswer("a", "b"),
                userAnswerCorrect = TranslateWithEndingsResult(false, false),
                callbacks = QuizCallbacksWithEndingsFake,
            )
        }
    }
}

@VisibleForTesting
private object QuizCallbacksWithEndingsFake : QuizCallbacks<UserAnswer.TranslateWithEndingsAnswer> {
    override fun checkAnswer(input: UserAnswer.TranslateWithEndingsAnswer) {}
    override fun nextWord() {}
    override fun toggleFavorite(isFavorite: Boolean) {}
    override fun returnToPreviousQuestion() {}
}
