package de.ywegel.svenska.ui.addEdit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import de.ywegel.svenska.ui.addEdit.models.userFacingString
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.common.vocabulary.abbreviation
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun WordGroupSelection(
    selectedGroup: ViewWordGroup?,
    selectedSubgroup: ViewWordSubGroup,
    onGroupSelected: (ViewWordGroup) -> Unit,
    onSubgroupSelected: (ViewWordSubGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        // TODO: Animate chips
        ChipRow(
            items = ViewWordGroup.entries,
            selectedItem = selectedGroup,
            onItemSelected = onGroupSelected,
            labelProvider = { it.userFacingString() },
        )

        VerticalSpacerXS()

        when (selectedGroup) {
            ViewWordGroup.Noun -> ChipRow(
                items = WordGroup.NounSubgroup.entries,
                selectedItem = (selectedSubgroup as? ViewWordSubGroup.Noun)?.type,
                onItemSelected = { onSubgroupSelected(ViewWordSubGroup.Noun(it)) },
                labelProvider = { it.selectorAbbreviation() },
            )

            ViewWordGroup.Verb -> ChipRow(
                items = WordGroup.VerbSubgroup.entries,
                selectedItem = (selectedSubgroup as? ViewWordSubGroup.Verb)?.type,
                onItemSelected = { onSubgroupSelected(ViewWordSubGroup.Verb(it)) },
                labelProvider = { it.selectorAbbreviation() },
            )

            else -> {}
        }
    }
}

@Composable
private fun <T> ChipRow(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    labelProvider: @Composable (T) -> String,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacings.xs),
        contentPadding = PaddingValues(horizontal = Spacings.xxs),
    ) {
        items(items) { item ->
            // TODO: Animate chip entry
            val isSelected = selectedItem == item
            FilterChip(
                selected = isSelected,
                onClick = { onItemSelected(item) },
                label = { Text(labelProvider(item)) },
                leadingIcon = {
                    if (isSelected) {
                        Icon(
                            imageVector = SvenskaIcons.Done,
                            contentDescription = null,
                        )
                    }
                },
            )
        }
    }
}

// Special abbreviations, making it clearer to the user what he is selecting, without cluttering the ui
@ReadOnlyComposable
@Composable
private fun WordGroup.NounSubgroup.selectorAbbreviation(): String {
    return when (this) {
        WordGroup.NounSubgroup.UNCHANGED_ETT -> R.string.addEdit_group_selector_noun_title_unchanged_ett
        WordGroup.NounSubgroup.UNCHANGED_EN -> R.string.addEdit_group_selector_noun_title_unchanged_en
        WordGroup.NounSubgroup.SPECIAL -> R.string.addEdit_group_selector_noun_title_special
        WordGroup.NounSubgroup.UNDEFINED -> R.string.addEdit_group_selector_noun_title_undefined
        else -> null
    }?.let { stringResource(it) } ?: this.abbreviation()
}

// Special abbreviations, making it clearer to the user what he is selecting, without cluttering the ui
@ReadOnlyComposable
@Composable
private fun WordGroup.VerbSubgroup.selectorAbbreviation(): String {
    return when (this) {
        WordGroup.VerbSubgroup.GROUP_4_SPECIAL -> R.string.addEdit_group_selector_verb_title_special
        WordGroup.VerbSubgroup.UNDEFINED -> R.string.addEdit_group_selector_verb_title_undefined
        else -> null
    }?.let { stringResource(it) } ?: this.abbreviation()
}

@Preview(showBackground = true)
@Composable
private fun Preview_NounSubgroupChips() {
    SvenskaTheme {
        ChipRow(
            items = WordGroup.NounSubgroup.entries,
            selectedItem = WordGroup.NounSubgroup.UNDEFINED,
            onItemSelected = { },
            labelProvider = { it.abbreviation() },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview_WordGroupSelection() {
    SvenskaTheme {
        WordGroupSelection(
            selectedGroup = ViewWordGroup.Verb,
            onGroupSelected = {},
            selectedSubgroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_2A),
            onSubgroupSelected = { },
        )
    }
}
