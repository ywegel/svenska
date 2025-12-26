package de.ywegel.svenska.ui.quiz.wordGroupQuiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerS
import de.ywegel.svenska.ui.common.vocabulary.abbreviation
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun QuizContent(
    innerPadding: PaddingValues,
    uiState: WordGroupQuizUiState.QuizItemState,
    onSubgroupSelected: (WordGroup.NounSubgroup) -> Unit,
    onNextClicked: () -> Unit,
) {
    uiState.userAnswerCorrect?.let { userAnswerCorrect ->
        WordGroupQuizSolutionSheet(
            userSolutionCorrect = userAnswerCorrect,
            vocabulary = uiState.vocabulary,
            navigateToNextQuestion = onNextClicked,
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = Spacings.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WordCard(uiState.vocabulary.word)

        NounSubgroupSelection(
            selectedSubgroup = uiState.selectedSubgroup,
            onSubgroupSelected = onSubgroupSelected,
        )
    }
}

@Composable
private fun WordCard(word: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = Spacings.xxl),
        shape = SvenskaTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = SvenskaTheme.colors.surfaceContainer,
        ),
    ) {
        Text(
            text = word,
            style = SvenskaTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacings.xxxl),
        )
    }
}

@Composable
private fun ColumnScope.NounSubgroupSelection(
    selectedSubgroup: WordGroup.NounSubgroup?,
    onSubgroupSelected: (WordGroup.NounSubgroup) -> Unit,
) {
    Column(modifier = Modifier.weight(1f)) {
        val mainSubgroups = listOf(
            WordGroup.NounSubgroup.OR,
            WordGroup.NounSubgroup.AR,
            WordGroup.NounSubgroup.ER,
            WordGroup.NounSubgroup.R,
            WordGroup.NounSubgroup.N,
            WordGroup.NounSubgroup.UNCHANGED_ETT,
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(Spacings.m),
            verticalArrangement = Arrangement.spacedBy(Spacings.m),
        ) {
            items(mainSubgroups) { subgroup ->
                val label = when (subgroup) {
                    WordGroup.NounSubgroup.UNCHANGED_ETT -> stringResource(R.string.groupQuiz_group_selection_unchanged)
                    else -> subgroup.abbreviation()
                }
                SubGroupButton(
                    label = label,
                    subgroup = subgroup,
                    selectedSubgroup = selectedSubgroup,
                    onSubgroupSelected = onSubgroupSelected,
                )
            }
        }

        VerticalSpacerS()

        SpecialGroupButton(
            selectedSubgroup = selectedSubgroup,
            onSubgroupSelected = onSubgroupSelected,
        )

        VerticalSpacerM()
    }
}

@Composable
private fun SubGroupButton(
    subgroup: WordGroup.NounSubgroup,
    label: String,
    selectedSubgroup: WordGroup.NounSubgroup?,
    onSubgroupSelected: (WordGroup.NounSubgroup) -> Unit,
) {
    val selected = selectedSubgroup == subgroup
    SelectableChip(
        label = label,
        selected = selected,
        onClick = { onSubgroupSelected(subgroup) },
        shape = SvenskaTheme.shapes.large,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        textStyle = SvenskaTheme.typography.titleLarge,
    )
}

@Composable
private fun SpecialGroupButton(
    selectedSubgroup: WordGroup.NounSubgroup?,
    onSubgroupSelected: (WordGroup.NounSubgroup) -> Unit,
) {
    val selected = selectedSubgroup == WordGroup.NounSubgroup.SPECIAL ||
        selectedSubgroup == WordGroup.NounSubgroup.UNDEFINED

    SelectableChip(
        label = stringResource(R.string.groupQuiz_group_selection_special),
        selected = selected,
        onClick = { onSubgroupSelected(WordGroup.NounSubgroup.SPECIAL) },
        shape = SvenskaTheme.shapes.medium,
        textStyle = SvenskaTheme.typography.bodyLarge,
        labelPadding = Spacings.xs,
    )
}

@Composable
private fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = SvenskaTheme.shapes.large,
    textStyle: TextStyle = SvenskaTheme.typography.titleLarge,
    labelPadding: Dp = 0.dp,
) {
    FilterChip(
        onClick = onClick,
        selected = selected,
        label = {
            Text(
                text = label,
                style = textStyle,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = labelPadding),
            )
        },
        modifier = modifier,
        shape = shape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = SvenskaTheme.colors.surface,
            labelColor = SvenskaTheme.colors.onSurface,
            selectedContainerColor = SvenskaTheme.colors.primaryContainer,
            selectedLabelColor = SvenskaTheme.colors.onPrimaryContainer,
        ),
    )
}
