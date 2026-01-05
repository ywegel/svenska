@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.quiz.wordGroupQuiz

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import de.ywegel.svenska.ui.common.FixedModalBottomSheet
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
    FixedModalBottomSheet(containerColor = containerColor) {
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

    Column(Modifier.padding(horizontal = Spacings.xl)) {
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

        val translation = remember(vocabulary) {
            val subgroup = (vocabulary.wordGroup as WordGroup.Noun).subgroup
            if (subgroup != WordGroup.NounSubgroup.SPECIAL && subgroup != WordGroup.NounSubgroup.UNDEFINED) {
                vocabulary.translation
            } else {
                "${vocabulary.translation} (${vocabulary.ending})"
            }
        }
        Text(stringResource(R.string.groupQuiz_prompt_translates_to, translation))

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

@Preview
@Composable
private fun WrongWithEndingsPreview() {
    SvenskaTheme {
        WordGroupQuizSolutionSheet(
            userSolutionCorrect = false,
            vocabulary = vocabulary(wordGroup = WordGroup.Noun(WordGroup.NounSubgroup.SPECIAL)),
            navigateToNextQuestion = {},
        )
    }
}
