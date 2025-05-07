package de.ywegel.svenska.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.WordImporterScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.search.OnlineSearchType
import de.ywegel.svenska.navigation.SettingsNavGraph
import de.ywegel.svenska.navigation.transitions.LateralTransition
import de.ywegel.svenska.ui.common.ClickableText
import de.ywegel.svenska.ui.common.HorizontalSpacerM
import de.ywegel.svenska.ui.common.SwitchWithText
import de.ywegel.svenska.ui.common.TopAppTextBar
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.search.userFacingTitle
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<SettingsNavGraph>(start = true, style = LateralTransition::class)
@Composable
fun SettingsScreen(navigator: DestinationsNavigator, viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onOverviewShowCompactVocabularyItemChanged = viewModel::toggleOverviewShowCompactVocabularyItem,
        onSearchShowCompactVocabularyItemChanged = viewModel::toggleSearchShowCompactVocabularyItem,
        onOnlineSearchTypeSelected = viewModel::onOnlineSearchTypeSelected,
        navigateUp = navigator::navigateUp,
        navigateToWordImporter = { navigator.navigate(WordImporterScreenDestination) },
    )
}

@Composable
private fun SettingsScreen(
    uiState: SettingsUiState,
    onOverviewShowCompactVocabularyItemChanged: (Boolean) -> Unit,
    onSearchShowCompactVocabularyItemChanged: (Boolean) -> Unit,
    onOnlineSearchTypeSelected: (OnlineSearchType) -> Unit,
    navigateUp: () -> Unit,
    navigateToWordImporter: () -> Unit,
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
                onCheckedChange = onOverviewShowCompactVocabularyItemChanged,
            )

            SwitchWithText(
                title = stringResource(R.string.settings_search_compact_vocabulary_item_title),
                description = stringResource(R.string.settings_search_compact_vocabulary_item_description),
                checked = uiState.searchShowCompactVocabularyItem,
                onCheckedChange = onSearchShowCompactVocabularyItemChanged,
            )

            VerticalSpacerM()

            ClickableText(
                title = stringResource(R.string.settings_naviagate_word_importer_screen_title),
                description = stringResource(R.string.settings_naviagate_word_importer_screen_description),
                onClick = navigateToWordImporter,
            )

            VerticalSpacerM()

            Column(Modifier.padding(horizontal = Spacings.m)) {
                OnlineRedirectSelector(uiState, onOnlineSearchTypeSelected)
            }
        }
    }
}

@Composable
private fun OnlineRedirectSelector(uiState: SettingsUiState, onOnlineSearchTypeSelected: (OnlineSearchType) -> Unit) {
    onlineSearchTypes.forEach { entry ->
        OnlineRedirectSelectorButton(
            type = entry,
            selected = entry == uiState.selectedOnlineSearchType,
            onTypeSelected = { onOnlineSearchTypeSelected(it) },
        ) {
            Text(
                text = entry.userFacingTitle(),
                style = SvenskaTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
    OnlineRedirectSelectorButton(
        type = uiState.selectedOnlineSearchType ?: OnlineSearchType.Custom(""),
        selected = uiState.selectedOnlineSearchType is OnlineSearchType.Custom,
        onTypeSelected = { onOnlineSearchTypeSelected(it) },
    ) {
        var inputUrl by remember { mutableStateOf("") }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .onFocusEvent {
                    if (it.hasFocus || it.isFocused) {
                        onOnlineSearchTypeSelected(
                            OnlineSearchType.Custom(""),
                        )
                    }
                },
            value = inputUrl,
            onValueChange = {
                inputUrl = it
                onOnlineSearchTypeSelected(OnlineSearchType.Custom(inputUrl))
            },
            label = { Text(stringResource(R.string.search_base_url_custom)) },
        )
    }
}

@Composable
private fun OnlineRedirectSelectorButton(
    type: OnlineSearchType,
    selected: Boolean,
    onTypeSelected: (OnlineSearchType) -> Unit,
    content: @Composable () -> Unit,
) {
    Row(
        Modifier
            .selectable(
                selected = selected,
                onClick = { onTypeSelected(type) },
                role = Role.RadioButton,
            )
            .fillMaxWidth()
            .padding(Spacings.xxs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = null, // null recommended for accessibility with screenreaders
        )

        HorizontalSpacerM()

        content()
    }
}

private val onlineSearchTypes = listOf(
    OnlineSearchType.DictCC,
    OnlineSearchType.Pons,
    OnlineSearchType.DeepL,
    OnlineSearchType.GoogleTranslate,
)

@Preview
@Composable
private fun SettingsScreenPreview() {
    SvenskaTheme {
        SettingsScreen(
            uiState = SettingsUiState(true, true, OnlineSearchType.Pons),
            onOverviewShowCompactVocabularyItemChanged = {},
            onSearchShowCompactVocabularyItemChanged = {},
            onOnlineSearchTypeSelected = {},
            navigateUp = {},
            navigateToWordImporter = {},
        )
    }
}
