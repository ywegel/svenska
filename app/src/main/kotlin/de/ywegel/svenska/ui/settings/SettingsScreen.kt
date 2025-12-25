@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.settings

import android.content.Intent
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.generated.destinations.AboutLibrariesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OnboardingScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SearchSettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.WordImporterScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.BuildConfig
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.search.OnlineSearchType
import de.ywegel.svenska.navigation.SettingsNavGraph
import de.ywegel.svenska.navigation.transitions.LateralTransition
import de.ywegel.svenska.ui.common.ClickableText
import de.ywegel.svenska.ui.common.SwitchWithText
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<SettingsNavGraph>(
    start = true,
    style = LateralTransition::class,
    deepLinks = [
        DeepLink(
            action = Intent.ACTION_APPLICATION_PREFERENCES, // "android.intent.action.APPLICATION_PREFERENCES",
        ),
    ],
)
@Composable
fun SettingsScreen(navigator: DestinationsNavigator, viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current

    SettingsScreen(
        uiState = uiState,
        callbacks = viewModel,
        navigateUp = {
            // If we got here via the deep link, we can only get back to the OS-Settings via the back dispatcher.
            backDispatcher?.onBackPressedDispatcher?.onBackPressed() ?: navigator.navigateUp()
        },
        navigateToWordImporter = { navigator.navigate(WordImporterScreenDestination) },
        navigateToAboutLibraries = { navigator.navigate(AboutLibrariesScreenDestination) },
        navigateToOnboarding = { navigator.navigate(OnboardingScreenDestination) },
        navigateToSearchSettings = { navigator.navigate(SearchSettingsScreenDestination) },
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    callbacks: SettingsCallbacks,
    navigateUp: () -> Unit,
    navigateToWordImporter: () -> Unit,
    navigateToAboutLibraries: () -> Unit,
    navigateToOnboarding: () -> Unit,
    navigateToSearchSettings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppTextBar(
                title = stringResource(R.string.settings_title),
                onNavigateUp = navigateUp,
                navigationIcon = Icons.AutoMirrored.Default.ArrowBack,
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            SwitchWithText(
                title = stringResource(R.string.settings_overview_compact_vocabulary_item_title),
                description = stringResource(R.string.settings_overview_compact_vocabulary_item_description),
                checked = uiState.overviewShowCompactVocabularyItem,
                onCheckedChange = callbacks::toggleOverviewShowCompactVocabularyItem,
            )

            VerticalSpacerXS()

            SwitchWithText(
                title = stringResource(R.string.settings_app_use_new_quiz_title),
                description = stringResource(R.string.settings_app_use_new_quiz_description),
                checked = uiState.appUseNewQuiz,
                onCheckedChange = callbacks::toggleUseNewQuiz,
            )

            VerticalSpacerXS()

            ClickableText(
                title = stringResource(R.string.settings_search_settings_title),
                description = stringResource(R.string.settings_search_settings_description),
                onClick = navigateToSearchSettings,
            )

            VerticalSpacerXS()

            ClickableText(
                title = stringResource(R.string.settings_naviagate_word_importer_screen_title),
                description = stringResource(R.string.settings_naviagate_word_importer_screen_description),
                onClick = navigateToWordImporter,
            )

            VerticalSpacerXS()

            ClickableText(
                title = stringResource(R.string.settings_show_onboarding_title),
                description = stringResource(R.string.settings_show_onboarding_description),
                onClick = navigateToOnboarding,
            )

            VerticalSpacerXXS()
            HorizontalDivider()
            VerticalSpacerXXS()

            AppInformationSection(navigateToAboutLibraries = navigateToAboutLibraries)
        }
    }
}

@Composable
private fun AppInformationSection(navigateToAboutLibraries: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val githubRepoUrl = stringResource(R.string.settings_github_repository_url)

    ClickableText(
        title = stringResource(R.string.settings_naviagate_about_libraries_screen_title),
        onClick = navigateToAboutLibraries,
    )

    ClickableText(
        title = stringResource(R.string.settings_github_repository),
        onClick = {
            uriHandler.openUri(githubRepoUrl)
        },
    )

    Text(
        text = stringResource(R.string.settings_app_version, BuildConfig.VERSION_NAME),
        style = SvenskaTheme.typography.bodyMedium,
        modifier = Modifier.padding(horizontal = Spacings.m, vertical = Spacings.s),
    )
}

@Preview
@Composable
private fun SettingsScreenPreview() {
    SvenskaTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                overviewShowCompactVocabularyItem = true,
                appUseNewQuiz = true,
                selectedOnlineSearchType = OnlineSearchType.Pons,
            ),
            callbacks = SettingsCallbacksFake,
            navigateUp = {},
            navigateToWordImporter = {},
            navigateToAboutLibraries = {},
            navigateToOnboarding = {},
            navigateToSearchSettings = {},
        )
    }
}
