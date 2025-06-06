@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.settings.subscreens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.domain.search.OnlineSearchType
import de.ywegel.svenska.navigation.SettingsNavGraph
import de.ywegel.svenska.ui.common.*
import de.ywegel.svenska.ui.search.userFacingTitle
import de.ywegel.svenska.ui.settings.SettingsCallbacks
import de.ywegel.svenska.ui.settings.SettingsCallbacksFake
import de.ywegel.svenska.ui.settings.SettingsUiState
import de.ywegel.svenska.ui.settings.SettingsViewModel
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<SettingsNavGraph>
@Composable
fun SearchSettingsScreen(navigator: DestinationsNavigator, viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SearchSettingsScreen(
        uiState = uiState,
        callbacks = viewModel,
        navigateUp = navigator::navigateUp,
    )
}

@Composable
private fun SearchSettingsScreen(
    uiState: SettingsUiState,
    callbacks: SettingsCallbacks,
    navigateUp: () -> Unit = {},
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
        Column(Modifier.padding(innerPadding).padding(horizontal = Spacings.m)) {
            SwitchWithText(
                title = stringResource(R.string.settings_search_compact_vocabulary_item_title),
                description = stringResource(R.string.settings_search_compact_vocabulary_item_description),
                checked = uiState.searchShowCompactVocabularyItem,
                onCheckedChange = callbacks::toggleSearchShowCompactVocabularyItem,
            )

            VerticalSpacerXS()

            HorizontalDivider()

            VerticalSpacerXS()

            Text(stringResource(R.string.settings_search_online_redirect_title))

            VerticalSpacerS()

            OnlineRedirectSelector(uiState, callbacks::onOnlineSearchTypeSelected)
        }
    }
}

@Composable
private fun OnlineRedirectSelector(uiState: SettingsUiState, onOnlineSearchTypeSelected: (OnlineSearchType) -> Unit) {
    var customInputUrl by remember { mutableStateOf("") }

    LaunchedEffect(uiState.selectedOnlineSearchType) {
        if (uiState.selectedOnlineSearchType != OnlineSearchType.Custom) {
            customInputUrl = uiState.selectedOnlineSearchType?.toString().orEmpty()
        }
    }

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
            value = customInputUrl,
            onValueChange = {
                customInputUrl = it
                onOnlineSearchTypeSelected(OnlineSearchType.Custom(customInputUrl))
            },
            label = {
                Text(stringResource(R.string.search_base_url_custom))
            },
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
            onClick = null,
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
fun SearchSettingsScreenPreview() {
    SvenskaTheme {
        SearchSettingsScreen(
            uiState = SettingsUiState(
                searchShowCompactVocabularyItem = true,
                selectedOnlineSearchType = OnlineSearchType.Pons,
            ),
            callbacks = SettingsCallbacksFake,
        )
    }
}
