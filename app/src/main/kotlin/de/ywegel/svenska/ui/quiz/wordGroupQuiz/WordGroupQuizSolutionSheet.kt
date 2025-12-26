@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.quiz.wordGroupQuiz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.data.model.vocabulary
import de.ywegel.svenska.ui.common.VerticalSpacerS
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.common.vocabulary.subGroupAbbreviation
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun WordGroupQuizSolutionSheet(
    userSolutionCorrect: Boolean,
    vocabulary: Vocabulary,
    navigateToNextQuestion: () -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    LaunchedEffect(vocabulary.id) {
        if (userSolutionCorrect) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Confirm)
        } else {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.Reject)
        }
    }

    val containerColor = if (userSolutionCorrect) {
        BottomSheetDefaults.ContainerColor
    } else {
        SvenskaTheme.colors.errorContainer
    }
    ModalBottomSheet(
        onDismissRequest = { /* Modal should not be hideable */ },
        dragHandle = {},
        properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false), // Modal should not be hideable
        containerColor = containerColor,
        sheetState = rememberModalBottomSheetState(
            confirmValueChange = {
                // Modal should not be hideable
                false
            },
        ),
    ) {
        SheetContent(
            userSolutionCorrect = userSolutionCorrect,
            vocabulary = vocabulary,
            navigateToNextQuestion = navigateToNextQuestion,
        )
    }
}

@Composable
private fun SheetContent(userSolutionCorrect: Boolean, vocabulary: Vocabulary, navigateToNextQuestion: () -> Unit) {
    val buttonColors = if (userSolutionCorrect) {
        ButtonDefaults.buttonColors()
    } else {
        ButtonDefaults.buttonColors(containerColor = SvenskaTheme.colors.error)
    }

    Column(
        Modifier
            .padding(horizontal = Spacings.xl, vertical = Spacings.s)
            .navigationBarsPadding(),
    ) {
        VerticalSpacerS()
        Text(
            style = SvenskaTheme.typography.headlineMedium,
            text = if (userSolutionCorrect) {
                stringResource(R.string.groupQuiz_correct)
            } else {
                stringResource(R.string.groupQuiz_wrong)
            },
        )
        if (!userSolutionCorrect) {
            VerticalSpacerXXS()
            Text(
                buildAnnotatedString {
                    append(stringResource(R.string.groupQuiz_correct_answer_hint))
                    pushStyle(SpanStyle(color = SvenskaTheme.colors.error))
                    append(vocabulary.wordGroup.subGroupAbbreviation())
                },
            )
        }
        VerticalSpacerXXS()

        Text(stringResource(R.string.groupQuiz_prompt_translates_to, vocabulary.translation))

        VerticalSpacerXS()

        Button(
            colors = buttonColors,
            onClick = navigateToNextQuestion,
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(stringResource(R.string.general_next))
        }
    }
}

@Preview
@Composable
private fun CorrectPreview() {
    SvenskaTheme {
        WordGroupQuizSolutionSheet(
            userSolutionCorrect = true,
            vocabulary = vocabulary(wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.ER)),
            navigateToNextQuestion = {},
        )
    }
}

@Preview
@Composable
private fun WrongPreview() {
    SvenskaTheme {
        WordGroupQuizSolutionSheet(
            userSolutionCorrect = false,
            vocabulary = vocabulary(wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.ER)),
            navigateToNextQuestion = {},
        )
    }
}
