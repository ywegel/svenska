package de.ywegel.svenska.ui.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.VerticalSpacerS
import de.ywegel.svenska.ui.common.VerticalSpacerXL
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun QuizErrorScreen(innerPadding: PaddingValues, exception: Exception?, onRetry: () -> Unit, onBack: (() -> Unit)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(Spacings.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.quiz_error_title),
            style = SvenskaTheme.typography.headlineMedium,
            color = SvenskaTheme.colors.error,
            textAlign = TextAlign.Center,
        )

        VerticalSpacerXS()

        Text(
            text = stringResource(R.string.quiz_error_error_message),
            style = SvenskaTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )

        if (exception != null) {
            VerticalSpacerS()
            Text(
                text = exception.localizedMessage ?: exception.toString(),
                style = SvenskaTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = SvenskaTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = Spacings.m),
            )
        }

        VerticalSpacerXL()

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.general_retry))
        }

        VerticalSpacerS()
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.quiz_error_back_to_overview))
        }
    }
}
