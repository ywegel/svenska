package de.ywegel.svenska.ui.bonus.numbers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.NavigationBarSpacer
import de.ywegel.svenska.ui.common.TabsScaffold
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<RootGraph>
@Composable
fun SwedishNumbersScreen(navigator: DestinationsNavigator, viewModel: SwedishNumbersViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    SwedishNumbersScreen(uiState = uiState, onNavigateUp = navigator::navigateUp)
}

@Composable
fun SwedishNumbersScreen(uiState: SwedishNumbersUiState, onNavigateUp: () -> Unit) {
    TabsScaffold(
        toolbarTitle = R.string.numbers_title,
        tabTitleResources = listOf(R.string.numbers_tab_small_numbers, R.string.numbers_tab_base_ten_numbers),
        pages = listOf(
            { NumbersGrid(uiState.regularNumbers) },
            { NumbersGrid(uiState.tensNumbers) },
        ),
        onNavigateUp = onNavigateUp,
    )
}

@Composable
fun NumbersGrid(numbers: List<NumberItem>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacings.xs),
        verticalArrangement = Arrangement.spacedBy(Spacings.xs),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = Spacings.xs,
            start = Spacings.xs,
            end = Spacings.xs,
        ),
    ) {
        items(numbers) { numberItem ->
            NumberCard(
                number = numberItem.number,
                swedishNumber = numberItem.annotatedString,
            )
        }
        item {
            NavigationBarSpacer()
        }
        if (numbers.size % 2 != 0) {
            item {
                NavigationBarSpacer()
            }
        }
    }
}

@Composable
fun NumberCard(number: Int, swedishNumber: AnnotatedString, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .padding(Spacings.m)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = number.toString(),
                style = SvenskaTheme.typography.headlineSmall,
            )
            VerticalSpacerXXS()
            Text(
                text = swedishNumber,
                style = SvenskaTheme.typography.bodyLarge,
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SwedishNumbersScreenPreview() {
    SvenskaTheme {
        SwedishNumbersScreen(
            SwedishNumbersUiState(
                emptyList(),
            ),
        ) {}
    }
}
