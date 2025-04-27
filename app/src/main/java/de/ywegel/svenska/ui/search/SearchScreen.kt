@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.ui.common.HorizontalSpacerM
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.common.NavigationIconButton
import de.ywegel.svenska.ui.overview.VocabularyItemCompact
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme
import java.util.LinkedList
import java.util.Queue

@Destination(navArgsDelegate = SearchScreenNavArgs::class)
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
) {
    Scaffold(
        topBar = {
            SearchToolbar(
                currentSearchQuery = currentSearchQuery,
                onSearchChanged = onSearchChanged,
                onSearch = onSearch,
                navigateUp = navigateUp,
            )
        },
    ) { contentPadding ->
        ItemList(
            outerPadding = contentPadding,
            vocabulary = vocabulary,
            lastSearchedItems = uiState.lastSearchedItems,
            searchQuery = currentSearchQuery,
        )
    }
}

@Composable
private fun ItemList(
    outerPadding: PaddingValues,
    vocabulary: List<Vocabulary>,
    lastSearchedItems: Queue<String>,
    searchQuery: String,
    onItemClicked: (Vocabulary) -> Unit = {},
    onRecentSearchedClicked: (String) -> Unit = {},
) {
    if (searchQuery.isBlank()) {
        Column(modifier = Modifier.padding(outerPadding)) {
            lastSearchedItems.forEach { lastSearch ->
                LastSearchedItem(lastSearch, onRecentSearchedClicked)
            }
        }
        if (lastSearchedItems.isEmpty()) {
            Text(
                text = stringResource(R.string.overview_search_no_searches),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(horizontal = Spacings.m, vertical = Spacings.l)
                    .fillMaxWidth(),
            )
        }
    } else {
        LazyColumn(contentPadding = outerPadding) {
            items(vocabulary, key = { it.id }) { item ->
                VocabularyItemCompact(vocabulary = item, modifier = Modifier.animateItem()) {
                    onItemClicked(it)
                }
            }
            item {
                // TODO: Search on dict.cc
            }
        }
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
        title = {
            // TODO: Style
            // TODO: search icon
            TextField(
                value = currentSearchQuery,
                onValueChange = onSearchChanged,
                label = { Text(stringResource(R.string.search_search_label)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                ),
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

data class SearchScreenNavArgs(
    val containerId: Int? = null,
)

@Preview
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
                lastSearchedItems = LinkedList(listOf("abc", "def", "ghi")) as Queue<String>,
            ),
            vocabulary = emptyList(),
            currentSearchQuery = "",
            onSearchChanged = {},
            onSearch = {},
            navigateUp = {},
        )
    }
}

@Preview
@Composable
private fun SearchScreenPreviewFilled() {
    SvenskaTheme {
        SearchScreen(
            uiState = SearchUiState(
                lastSearchedItems = LinkedList(emptyList<String>()) as Queue<String>,
            ),
            vocabulary = emptyList(),
            currentSearchQuery = "abc",
            onSearchChanged = {},
            onSearch = {},
            navigateUp = {},
        )
    }
}
