@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.search

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowOutward
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.EditVocabularyScreenDestination
import com.ramcosta.composedestinations.generated.destinations.WordGroupsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.vocabularies
import de.ywegel.svenska.navigation.SvenskaGraph
import de.ywegel.svenska.navigation.transitions.LateralTransition
import de.ywegel.svenska.ui.common.HorizontalSpacerM
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.common.NavigationIconButton
import de.ywegel.svenska.ui.common.rememberColumnScaffoldInsets
import de.ywegel.svenska.ui.common.vocabulary.VocabularyList
import de.ywegel.svenska.ui.common.vocabulary.VocabularyListCallbacks
import de.ywegel.svenska.ui.common.vocabulary.VocabularyListCallbacksFake
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme
import kotlinx.coroutines.launch

private const val TAG = "SearchScreen"

@Destination<SvenskaGraph>(navArgs = SearchScreenNavArgs::class, style = LateralTransition::class)
@Composable
fun SearchScreen(navigator: DestinationsNavigator) {
    val viewModel = hiltViewModel<SearchViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val vocabulary by viewModel.vocabularyFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    SearchScreen(
        uiState = uiState,
        vocabulary = vocabulary,
        currentSearchQuery = searchQuery,
        onSearchChanged = viewModel::updateSearchQuery,
        onSearch = viewModel::onSearch,
        navigateUp = navigator::navigateUp,
        navigateToEdit = { item ->
            navigator.navigate(
                EditVocabularyScreenDestination(
                    containerId = item.containerId,
                    initialVocabulary = item,
                ),
            )
        },
        navigateToWordGroupScreen = { navigator.navigate(WordGroupsScreenDestination) },
        vocabularyListCallbacks = viewModel,
    )
}

/**
 * @param onSearchChanged is called everytime the search input changes
 * @param onSearch is only called if the user actively clicks the keyboard search button
 */
@Composable
private fun SearchScreen(
    uiState: SearchUiState,
    vocabulary: List<Vocabulary>,
    currentSearchQuery: String,
    onSearchChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    navigateUp: () -> Unit,
    vocabularyListCallbacks: VocabularyListCallbacks,
    navigateToEdit: (vocabulary: Vocabulary) -> Unit,
    navigateToWordGroupScreen: () -> Unit,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val uriHandle = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SearchToolbar(
                currentSearchQuery = currentSearchQuery,
                onSearchChanged = onSearchChanged,
                onSearch = onSearch,
                navigateUp = navigateUp,
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
    ) { contentPadding ->
        ItemList(
            outerPadding = contentPadding,
            vocabulary = vocabulary,
            uiState = uiState,
            searchQuery = currentSearchQuery,
            navigateToEdit = navigateToEdit,
            navigateToWordGroupScreen = navigateToWordGroupScreen,
            onRecentSearchedClicked = onSearch,
            onOnlineRedirectClicked = { baseUrl, query ->
                try {
                    uriHandle.openUri(baseUrl + query)
                } catch (e: IllegalArgumentException) {
                    Log.w(TAG, "SearchScreen: onOnlineRedirectClicked", e)
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar(context.getString(R.string.search_uri_parsing_error))
                    }
                }
            },
            vocabularyListCallbacks = vocabularyListCallbacks,
        )
    }
}

@Composable
private fun ItemList(
    outerPadding: PaddingValues,
    vocabulary: List<Vocabulary>,
    uiState: SearchUiState,
    searchQuery: String,
    vocabularyListCallbacks: VocabularyListCallbacks,
    navigateToEdit: (vocabulary: Vocabulary) -> Unit,
    navigateToWordGroupScreen: () -> Unit,
    onRecentSearchedClicked: (String) -> Unit = {},
    onOnlineRedirectClicked: (baseUrl: String, query: String) -> Unit,
) {
    if (searchQuery.isBlank()) {
        EmptySearchScreen(
            outerPadding = outerPadding,
            uiState = uiState,
            onRecentSearchedClicked = onRecentSearchedClicked,
        )
    } else {
        val outerPadding = rememberColumnScaffoldInsets(outerPadding, Spacings.xs)

        VocabularyList(
            vocabularies = vocabulary,
            showContainerInformation = true,
            vocabularyDetailState = uiState.detailViewState,
            headerItems = {
                if (uiState.showOnlineRedirectFirst && uiState.onlineRedirectUrl != null) {
                    item {
                        OnlineRedirectItem {
                            onOnlineRedirectClicked(uiState.onlineRedirectUrl, searchQuery)
                        }
                    }
                }
            },
            footerItems = {
                if (!uiState.showOnlineRedirectFirst && uiState.onlineRedirectUrl != null) {
                    item {
                        OnlineRedirectItem {
                            onOnlineRedirectClicked(uiState.onlineRedirectUrl, searchQuery)
                        }
                    }
                }
            },
            scrollBehavior = null,
            contentPadding = outerPadding,
            vocabularyListCallbacks = vocabularyListCallbacks,
            navigateToEdit = navigateToEdit,
            navigateToWordGroupScreen = navigateToWordGroupScreen,
        )
    }
}

@Composable
private fun EmptySearchScreen(
    outerPadding: PaddingValues,
    uiState: SearchUiState,
    onRecentSearchedClicked: (String) -> Unit = {},
) {
    Column(modifier = Modifier.padding(outerPadding)) {
        uiState.lastSearchedItems.forEach { lastSearch ->
            LastSearchedItem(lastSearch, onRecentSearchedClicked)
        }
    }
    if (uiState.lastSearchedItems.isEmpty()) {
        Text(
            text = stringResource(R.string.search_no_searches),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = Spacings.m, vertical = Spacings.l)
                .fillMaxWidth(),
        )
    }
}

@Composable
private fun LastSearchedItem(query: String, onItemClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(query) }
            .padding(vertical = Spacings.s, horizontal = Spacings.m),
    ) {
        Icon(SvenskaIcons.History, null)
        HorizontalSpacerM()
        Text(query)
    }
}

@Composable
private fun OnlineRedirectItem(onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .padding(horizontal = Spacings.xs)
            .padding(bottom = Spacings.m),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(Spacings.m),
        ) {
            Text("Search online", Modifier.weight(1f))
            Icon(SvenskaIcons.ArrowOutward, null)
        }
    }
}

@Composable
private fun SearchToolbar(
    currentSearchQuery: String,
    onSearchChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    navigateUp: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    // TODO: retest this and decide if it should be kept
    BackHandler(currentSearchQuery.isNotBlank()) {
        onSearchChanged("")
        focusManager.clearFocus(true)
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SvenskaTheme.colors.surfaceContainer),
        title = {
            SearchTextField(
                currentSearchQuery = currentSearchQuery,
                focusManager = focusManager,
                onSearchChanged = onSearchChanged,
                onSearch = onSearch,
            )
        },
        navigationIcon = {
            NavigationIconButton(
                onNavigateUp = {
                    // Clear user input and navigate up on second click
                    if (currentSearchQuery.isNotBlank()) {
                        onSearchChanged("")
                        focusManager.clearFocus(true)
                    } else {
                        navigateUp()
                    }
                },
                navigationIcon = Icons.AutoMirrored.Default.ArrowBack,
            )
        },
        actions = {
            if (currentSearchQuery.isNotBlank()) {
                IconButton(SvenskaIcons.Close, null) { onSearchChanged("") }
            }
        },
    )
}

@Composable
private fun SearchTextField(
    currentSearchQuery: String,
    focusManager: FocusManager,
    onSearchChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    BasicTextField(
        value = currentSearchQuery,
        onValueChange = onSearchChanged,
        modifier = Modifier
            .focusRequester(focusRequester)
            .padding(horizontal = Spacings.s),
        textStyle = searchBarTextStyle(SvenskaTheme.colors.onSurface),
        decorationBox = { innerTextField ->
            if (currentSearchQuery.isEmpty()) {
                Text(
                    text = stringResource(R.string.search_search_label),
                    style = searchBarTextStyle(SvenskaTheme.colors.onSurfaceVariant),
                )
            }
            innerTextField()
        },
        cursorBrush = SolidColor(SvenskaTheme.colors.primary),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                focusManager.clearFocus()
                onSearch(currentSearchQuery)
            },
        ),
    )
}

@ReadOnlyComposable
@Composable
private fun searchBarTextStyle(color: Color): TextStyle {
    return SvenskaTheme.typography.titleLarge.copy(
        fontSize = 20.sp,
        color = color,
    )
}

data class SearchScreenNavArgs(
    val containerId: Int? = null,
)

@PreviewLightDark
@Composable
private fun ToolbarPreviewEmpty() {
    SvenskaTheme {
        SearchToolbar("", {}, {}, {})
    }
}

@Preview
@Composable
private fun ToolbarPreviewFilled() {
    SvenskaTheme {
        SearchToolbar("asdf", {}, {}, {})
    }
}

@Preview
@Composable
private fun SearchScreenPreviewEmpty() {
    SvenskaTheme {
        SearchScreen(
            uiState = SearchUiState(
                lastSearchedItems = ArrayDeque(listOf("abc", "def", "ghi")),
            ),
            vocabulary = emptyList(),
            currentSearchQuery = "",
            onSearchChanged = {},
            onSearch = {},
            navigateUp = {},
            navigateToEdit = {},
            navigateToWordGroupScreen = {},
            vocabularyListCallbacks = VocabularyListCallbacksFake,
        )
    }
}

@VisibleForTesting
@PreviewLightDark
@Composable
private fun SearchScreenPreviewFilled() {
    SvenskaTheme {
        SearchScreen(
            uiState = SearchUiState(),
            vocabulary = emptyList(),
            currentSearchQuery = "abc",
            onSearchChanged = {},
            onSearch = {},
            navigateUp = {},
            navigateToEdit = {},
            navigateToWordGroupScreen = {},
            vocabularyListCallbacks = VocabularyListCallbacksFake,
        )
    }
}

@VisibleForTesting
@PreviewLightDark
@Composable
private fun SearchScreenPreviewFilledAndItemsAvailable() {
    SvenskaTheme {
        SearchScreen(
            uiState = SearchUiState(),
            vocabulary = vocabularies(),
            currentSearchQuery = "abc",
            onSearchChanged = {},
            onSearch = {},
            navigateUp = {},
            navigateToEdit = {},
            navigateToWordGroupScreen = {},
            vocabularyListCallbacks = VocabularyListCallbacksFake,
        )
    }
}

@Preview
@Composable
private fun OnlineRedirectItemPreview() {
    SvenskaTheme {
        OnlineRedirectItem {}
    }
}
