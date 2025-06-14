@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.quiz.configuration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.QuizScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.navigation.QuizGraph
import de.ywegel.svenska.navigation.transitions.LateralTransition
import de.ywegel.svenska.ui.common.HorizontalSpacerS
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<QuizGraph>(style = LateralTransition::class, start = true)
@Composable
fun QuizConfigurationScreen(navigator: DestinationsNavigator, containerId: Int?) {
    val viewModel = viewModel<QuizConfigurationViewModel>()
    val configState by viewModel.configurationState.collectAsStateWithLifecycle()

    QuizConfigurationScreen(
        configState = configState,
        callbacks = viewModel,
        navigateToQuiz = {
            viewModel.generateNavigationArgs()?.let { quizMode ->
                // navigator.popBackStack() // TODO: maybe remove config screen from backstack after navigation?
                navigator.navigate(
                    QuizScreenDestination(
                        quizMode = quizMode,
                        containerId = containerId,
                    ),
                )
            }
        },
        onNavigateUp = navigator::navigateUp,
    )
}

@Composable
private fun QuizConfigurationScreen(
    configState: ConfigurationState,
    callbacks: QuizConfigurationCallbacks,
    navigateToQuiz: () -> Unit,
    onNavigateUp: () -> Unit,
) {
    Scaffold(topBar = { TopAppTextBar("Configure the Quiz", onNavigateUp) }) { padding ->
        Column(
            Modifier.padding(padding),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TranslationModeSelector(
                    selectedMode = configState.selectedType,
                    enabled = !configState.onlyEndings,
                    selectedModeChanged = callbacks::quizModeChanged,
                )
                VerticalSpacerM()

                Column(Modifier.padding(Spacings.xl)) {
                    // TODO: Add explanations to both switches. Either by description text or an info icon with popup bubble
                    // TODO: Additionally rework the switches. Maybe use the same ones, as for the Settings ans explain these switches in the description?
                    SwitchWithText(
                        text = "Additionally test endings",
                        fillTextWidth = false,
                        switchChecked = configState.withEndings,
                        onSwitchChanged = callbacks::withEndingsChanged,
                    )

                    AnimatedVisibility(configState.withEndings) {
                        SwitchWithText(
                            text = "Only test endings",
                            fillTextWidth = false,
                            switchChecked = configState.onlyEndings,
                            onSwitchChanged = callbacks::onlyEndingsChanged,
                        )
                    }
                }
            }
            Button(
                onClick = navigateToQuiz,
                modifier = Modifier
                    .padding(Spacings.m)
                    .fillMaxWidth(),
                enabled = configState.selectedType != null || configState.onlyEndings,
            ) { Text(stringResource(R.string.general_next)) }
        }
    }
}

@Composable
private fun TranslationModeSelector(
    selectedMode: TranslateMode?,
    enabled: Boolean,
    selectedModeChanged: (TranslateMode) -> Unit,
) {
    Column(
        modifier = Modifier
            .width(IntrinsicSize.Min),
        verticalArrangement = Arrangement.Center,
    ) {
        TranslateMode.entries.forEach { mode ->
            if (mode == selectedMode) {
                FilledTonalButton(
                    onClick = { selectedModeChanged(mode) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                ) { Text(text = mode.userFacingString()) }
            } else {
                OutlinedButton(
                    onClick = { selectedModeChanged(mode) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                ) { Text(text = mode.userFacingString()) }
            }
        }
    }
}

@Composable
fun TranslateMode.userFacingString(): String {
    return when (this) {
        TranslateMode.Random -> stringResource(R.string.quiz_mode_random)
        TranslateMode.Swedish -> stringResource(R.string.quiz_mode_swedish)
        TranslateMode.Native -> stringResource(R.string.quiz_mode_native)
    }
}

@Composable
private fun SwitchWithText(
    text: String,
    switchChecked: Boolean,
    fillTextWidth: Boolean = true,
    onSwitchChanged: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(switchChecked, onSwitchChanged)
        HorizontalSpacerS()
        Text(
            modifier = if (fillTextWidth) Modifier.weight(1f) else Modifier,
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview
@Composable
private fun QuizConfigPreview() {
    SvenskaTheme {
        QuizConfigurationScreen(
            configState = ConfigurationState(TranslateMode.Native),
            callbacks = QuizConfigurationCallbacksFake,
            navigateToQuiz = {},
            onNavigateUp = {},
        )
    }
}

@Preview
@Composable
private fun QuizConfigExtendedPreview() {
    SvenskaTheme {
        QuizConfigurationScreen(
            configState = ConfigurationState(TranslateMode.Native, withEndings = true, onlyEndings = true),
            callbacks = QuizConfigurationCallbacksFake,
            navigateToQuiz = {},
            onNavigateUp = {},
        )
    }
}
