@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.quiz.configuration

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import de.ywegel.svenska.BuildConfig
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.quiz.model.TranslateMode
import de.ywegel.svenska.navigation.QuizGraph
import de.ywegel.svenska.navigation.transitions.LateralTransition
import de.ywegel.svenska.ui.common.HorizontalSpacerS
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

private const val TAG = "QuizConfigurationScreen"

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
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "QuizConfigurationScreen: Navigating to QuizScreen with navigation args: $quizMode")
                }
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
    Scaffold(topBar = { TopAppTextBar(stringResource(R.string.quiz_config_title), onNavigateUp) }) { padding ->
        Column(
            Modifier.padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                TranslationModeSelector(
                    selectedMode = configState.selectedMode,
                    enabled = !configState.onlyEndings,
                    selectedModeChanged = callbacks::quizModeChanged,
                )
                VerticalSpacerM()

                Column(Modifier.padding(Spacings.xl)) {
                    // TODO: Add explanations to both switches. Either by description text or an info icon with popup bubble
                    // TODO: Additionally rework the switches. Maybe use the same ones, as for the Settings ans explain these switches in the description?
                    SwitchWithText(
                        text = stringResource(R.string.quiz_config_additionally_test_endings_description),
                        fillTextWidth = false,
                        switchChecked = configState.withEndings,
                        enabled = !configState.onlyEndings,
                        onSwitchChanged = callbacks::withEndingsChanged,
                    )

                    SwitchWithText(
                        text = stringResource(R.string.quiz_config_only_test_endings_description),
                        fillTextWidth = false,
                        switchChecked = configState.onlyEndings,
                        onSwitchChanged = callbacks::onlyEndingsChanged,
                    )
                }
            }
            VerticalSpacerM()
            SwitchWithText(
                text = stringResource(R.string.quiz_config_shuffle_words_description),
                switchChecked = configState.shuffleWords,
                fillTextWidth = false,
                onSwitchChanged = callbacks::shuffleWordsChanged,
            )
            VerticalSpacerXXS()
            Button(
                onClick = navigateToQuiz,
                modifier = Modifier
                    .padding(start = Spacings.m, end = Spacings.m, bottom = Spacings.m)
                    .fillMaxWidth(),
                enabled = configState.selectedMode != null || configState.onlyEndings,
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
        TranslateMode.SwedishToNative -> stringResource(R.string.quiz_mode_swedish)
        TranslateMode.NativeToSwedish -> stringResource(R.string.quiz_mode_native)
    }
}

@Composable
private fun SwitchWithText(
    text: String,
    switchChecked: Boolean,
    fillTextWidth: Boolean = true,
    enabled: Boolean = true,
    onSwitchChanged: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = switchChecked, onCheckedChange = onSwitchChanged, enabled = enabled)
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
            configState = ConfigurationState(TranslateMode.SwedishToNative),
            callbacks = QuizConfigurationCallbacksFake,
            navigateToQuiz = {},
            onNavigateUp = {},
        )
    }
}

@Preview
@Composable
private fun QuizConfigWithEndingsPreview() {
    SvenskaTheme {
        QuizConfigurationScreen(
            configState = ConfigurationState(TranslateMode.SwedishToNative, withEndings = true, onlyEndings = false),
            callbacks = QuizConfigurationCallbacksFake,
            navigateToQuiz = {},
            onNavigateUp = {},
        )
    }
}

@Preview
@Composable
private fun QuizConfigOnlyEndingsPreview() {
    SvenskaTheme {
        QuizConfigurationScreen(
            configState = ConfigurationState(TranslateMode.NativeToSwedish, withEndings = true, onlyEndings = true),
            callbacks = QuizConfigurationCallbacksFake,
            navigateToQuiz = {},
            onNavigateUp = {},
        )
    }
}
