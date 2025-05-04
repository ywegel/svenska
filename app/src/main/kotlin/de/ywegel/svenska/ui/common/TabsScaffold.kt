@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch

@Composable
fun TabsScaffold(
    toolbarTitle: Int,
    tabTitleResources: List<Int>,
    pages: List<@Composable () -> Unit>,
    onNavigateUp: () -> Unit,
) {
    TabsScaffold(
        toolbarTitle = stringResource(toolbarTitle),
        tabTitles = tabTitleResources.map { stringResource(it) },
        pages = pages,
        onNavigateUp = onNavigateUp,
    )
}

@Composable
fun TabsScaffold(
    toolbarTitle: String,
    tabTitles: List<String>,
    pages: List<@Composable () -> Unit>,
    onNavigateUp: () -> Unit,
) {
    val pagerState = rememberPagerState { pages.size }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(toolbarTitle) },
                navigationIcon = {
                    NavigationIconButton(onNavigateUp = onNavigateUp)
                },
            )
        },
    ) { paddingValues ->
        val paddings = rememberColumnScaffoldInsets(paddingValues)

        Column(
            modifier = Modifier
                .padding(paddings)
                .fillMaxSize(),
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                modifier = Modifier.fillMaxWidth(),
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        text = { Text(title) },
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                key = { page -> tabTitles[page] + page.toString() },
            ) { page ->
                pages[page]()
            }
        }
    }
}
