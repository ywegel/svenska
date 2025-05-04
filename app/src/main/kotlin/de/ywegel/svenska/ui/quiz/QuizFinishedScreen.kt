package de.ywegel.svenska.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerXL
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme
import kotlin.math.roundToInt

@Composable
fun QuizFinishedScreen(
    innerPadding: PaddingValues,
    correctAnswers: Int,
    totalQuestions: Int,
    score: Float,
    onStartNewQuiz: () -> Unit,
    navigateUp: () -> Unit,
) {
    val percentage = (score * 100).roundToInt()
    val motivationText = when {
        percentage >= AWESOME -> stringResource(R.string.quiz_finished_motivation_awesome)
        percentage >= GOOD -> stringResource(R.string.quiz_finished_motivation_good_job)
        percentage >= OK -> stringResource(R.string.quiz_finished_motivation_keep_going)
        else -> stringResource(R.string.quiz_finished_motivation_dont_give_up)
    }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
            .padding(Spacings.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.quiz_finished_title),
                style = SvenskaTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
            )

            VerticalSpacerXL()

            QuizResultCard(correctAnswers, totalQuestions, percentage)

            VerticalSpacerM()

            Text(
                text = motivationText,
                style = SvenskaTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Spacings.m),
            )
        }

        Column {
            Button(
                onClick = onStartNewQuiz,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacings.s),
                shape = RoundedCornerShape(Spacings.m),
            ) {
                Text(stringResource(R.string.quiz_finished_start_new_quiz))
            }

            OutlinedButton(
                onClick = navigateUp,
                modifier = Modifier.fillMaxWidth(),
                // TODO: Maybe use this shape everywhere?
                shape = RoundedCornerShape(Spacings.m),
            ) {
                Text(stringResource(R.string.quiz_finished_back_to_overview))
            }
        }
    }
}

private const val OK = 50
private const val GOOD = 70
private const val AWESOME = 90

@Composable
private fun QuizResultCard(correctAnswers: Int, totalQuestions: Int, percentage: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Spacings.m),
        elevation = CardDefaults.cardElevation(6.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(Spacings.xl)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(
                    R.string.quiz_finished_correct_answers_label,
                    correctAnswers,
                    totalQuestions,
                ),
                style = SvenskaTheme.typography.headlineSmall,
            )

            Text(
                text = stringResource(R.string.quiz_finished_score_label, percentage),
                style = SvenskaTheme.typography.titleLarge.copy(
                    color = SvenskaTheme.colors.primary,
                ),
            )
        }
    }
}
