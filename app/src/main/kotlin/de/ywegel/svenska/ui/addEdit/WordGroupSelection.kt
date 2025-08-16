package de.ywegel.svenska.ui.addEdit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import de.ywegel.svenska.ui.addEdit.models.userFacingString
import de.ywegel.svenska.ui.common.vocabulary.abbreviation
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme
import kotlinx.coroutines.delay

@Composable
fun WordGroupSelection(
    selectedGroup: ViewWordGroup?,
    selectedSubgroup: ViewWordSubGroup,
    onGroupSelected: (ViewWordGroup) -> Unit,
    onSubgroupSelected: (ViewWordSubGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ChipRow(
            items = ViewWordGroup.entries,
            selectedItem = selectedGroup,
            onItemSelected = onGroupSelected,
            labelProvider = { it.userFacingString() },
        )

        // We store the last valid group (NOUN or VERB) to ensure content is still available during the exit animation.
        // This avoids breaking the animation when selectedGroup becomes something else (e.g. OTHER).
        var lastValidGroup by remember {
            mutableStateOf(
                if (selectedGroup == ViewWordGroup.Noun || selectedGroup == ViewWordGroup.Verb) selectedGroup else null,
            )
        }

        LaunchedEffect(selectedGroup) {
            if (selectedGroup == ViewWordGroup.Noun || selectedGroup == ViewWordGroup.Verb) {
                lastValidGroup = selectedGroup
            }
        }

        // AnimatedVisibility only runs when a valid group is selected AND lastValidGroup is not null.
        // This prevents visual glitches by ensuring a stable layout exists during both enter and exit animations.
        AnimatedVisibility(
            visible = lastValidGroup != null &&
                (selectedGroup == ViewWordGroup.Noun || selectedGroup == ViewWordGroup.Verb),
        ) {
            when (lastValidGroup) {
                ViewWordGroup.Noun -> ChipRow(
                    items = WordGroup.NounSubgroup.entries,
                    selectedItem = (selectedSubgroup as? ViewWordSubGroup.Noun)?.type,
                    onItemSelected = { onSubgroupSelected(ViewWordSubGroup.Noun(it)) },
                    labelProvider = { it.selectorAbbreviation() },
                    animate = false,
                )

                ViewWordGroup.Verb -> ChipRow(
                    items = WordGroup.VerbSubgroup.entries,
                    selectedItem = (selectedSubgroup as? ViewWordSubGroup.Verb)?.type,
                    onItemSelected = { onSubgroupSelected(ViewWordSubGroup.Verb(it)) },
                    labelProvider = { it.selectorAbbreviation() },
                    animate = false,
                )

                else -> {}
            }
        }
    }
}

@Composable
private fun <T> ChipRow(
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    animate: Boolean = true,
    labelProvider: @Composable (T) -> String,
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacings.xs),
        contentPadding = PaddingValues(horizontal = Spacings.m),
    ) {
        itemsIndexed(items) { index, group ->
            val isSelected = selectedItem == group
            AnimatedChipEntry(index = index, animate = animate) {
                FilterChip(
                    selected = isSelected,
                    onClick = { onItemSelected(group) },
                    label = { Text(labelProvider(group)) },
                    leadingIcon = {
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally(),
                        ) {
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
}

@Composable
private fun AnimatedChipEntry(
    index: Int,
    animate: Boolean = true,
    delayPerItem: Int = 40,
    content: @Composable () -> Unit,
) {
    if (!animate) {
        content()
        return
    }
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * delayPerItem.toLong())
        visible.value = true
    }

    AnimatedVisibility(
        visible = visible.value,
        enter = slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth / 2 + index * 10 },
        ) + fadeIn(),
        exit = ExitTransition.None,
    ) {
        content()
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
