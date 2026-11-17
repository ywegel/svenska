package de.ywegel.svenska.ui.addEdit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.common.vocabulary.article
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

private val GenderToggleWidth = 76.dp
private val TextFieldHeight = 56.dp

// OutlinedTextField reserves space above its border for the floating label
private val TextFieldLabelOffset = 8.dp

@Composable
fun WordSection(uiState: AddEditUiState, callbacks: AddEditVocabularyCallbacks, modifier: Modifier = Modifier) {
    val wordInput = uiState.wordInput

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacings.m)) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacings.xs)) {
            if (uiState.selectedWordGroup == ViewWordGroup.Noun) {
                GenderToggle(
                    gender = uiState.gender ?: Gender.defaultIfEmpty,
                    onGenderChange = callbacks::updateGender,
                    modifier = Modifier.padding(top = TextFieldLabelOffset),
                )
            }
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = uiState.wordWithAnnotation,
                onValueChange = callbacks::updateWordWithAnnotation,
                label = { Text(stringResource(R.string.addEdit_label_word)) },
                isError = wordInput.hasHighlightError,
                supportingText = {
                    val supportingText = if (wordInput.hasHighlightError) {
                        R.string.addEdit_word_highlight_error
                    } else {
                        R.string.addEdit_word_supporting_text
                    }
                    Text(stringResource(supportingText))
                },
            )
        }

        if (uiState.selectedWordGroup != ViewWordGroup.Other && wordInput.words.size > 1) {
            ChangingWordSelection(
                words = wordInput.words,
                changingIndex = wordInput.changingIndex,
                onSelect = callbacks::selectChangingWord,
            )
        }
    }
}

/** One tap switches between en and ett, there are only two options. */
@Composable
private fun GenderToggle(gender: Gender, onGenderChange: (Gender) -> Unit, modifier: Modifier = Modifier) {
    val other = if (gender == Gender.Ultra) Gender.Neutra else Gender.Ultra
    val description = stringResource(R.string.accessibility_addEdit_gender_toggle, gender.article(), other.article())

    Surface(
        onClick = { onGenderChange(other) },
        modifier = modifier
            .size(width = GenderToggleWidth, height = TextFieldHeight)
            .semantics { contentDescription = description },
        shape = RoundedCornerShape(Spacings.m),
        color = SvenskaTheme.colors.secondaryContainer,
        contentColor = SvenskaTheme.colors.onSecondaryContainer,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(gender.article(), style = SvenskaTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(SvenskaIcons.SwapHoriz, contentDescription = null, modifier = Modifier.size(12.dp))
                Text(other.article(), style = SvenskaTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChangingWordSelection(words: List<String>, changingIndex: Int?, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacings.xs)) {
        Text(
            text = stringResource(R.string.addEdit_changing_word_title),
            style = SvenskaTheme.typography.titleSmall,
            color = SvenskaTheme.colors.onSurfaceVariant,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacings.xs),
            verticalArrangement = Arrangement.spacedBy(Spacings.xs),
        ) {
            words.forEachIndexed { index, word ->
                val role = when {
                    changingIndex == null || index > changingIndex -> R.string.addEdit_changing_word_role_suffix
                    index < changingIndex -> R.string.addEdit_changing_word_role_prefix
                    else -> R.string.addEdit_changing_word_role_changes
                }
                WordChip(
                    word = word,
                    role = stringResource(role),
                    isChanging = index == changingIndex,
                    onClick = { onSelect(index) },
                )
            }
        }
    }
}

/** Shows the word and its role in one box, the role is the explanation of what tapping does. */
@Composable
private fun WordChip(word: String, role: String, isChanging: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.semantics { selected = isChanging },
        shape = RoundedCornerShape(Spacings.s),
        color = if (isChanging) SvenskaTheme.colors.primaryContainer else SvenskaTheme.colors.surfaceContainerHigh,
        contentColor = if (isChanging) SvenskaTheme.colors.onPrimaryContainer else SvenskaTheme.colors.onSurface,
    ) {
        Column(
            modifier = Modifier
                .defaultMinSize(minWidth = 72.dp)
                .padding(horizontal = Spacings.m, vertical = Spacings.xxs),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(word, style = SvenskaTheme.typography.bodyLarge)
            Text(role, style = SvenskaTheme.typography.labelSmall)
        }
    }
}
