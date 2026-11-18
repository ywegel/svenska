package de.ywegel.svenska.ui.addEdit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

/** Rarely used fields. They only take space once they have content or the user adds them. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtrasSection(uiState: AddEditUiState, callbacks: AddEditVocabularyCallbacks, modifier: Modifier = Modifier) {
    var showNotes by rememberSaveable { mutableStateOf(uiState.notes.isNotBlank()) }
    var showPronunciation by rememberSaveable { mutableStateOf(!uiState.irregularPronunciation.isNullOrBlank()) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacings.m)) {
        if (showNotes) {
            RemovableTextField(
                value = uiState.notes,
                onValueChange = callbacks::updateNotes,
                label = stringResource(R.string.addEdit_label_notes),
                removeContentDescription = stringResource(R.string.accessibility_addEdit_remove_notes),
                minLines = 2,
                onRemove = {
                    showNotes = false
                    callbacks.updateNotes("")
                },
            )
        }
        if (showPronunciation) {
            RemovableTextField(
                value = uiState.irregularPronunciation.orEmpty(),
                onValueChange = callbacks::updateIrregularPronunciation,
                label = stringResource(R.string.addEdit_label_irregular_pronunciation),
                removeContentDescription = stringResource(
                    R.string.accessibility_addEdit_remove_irregular_pronunciation,
                ),
                onRemove = {
                    showPronunciation = false
                    callbacks.updateIrregularPronunciation("")
                },
            )
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacings.xs)) {
            if (!showNotes) {
                AddFieldChip(label = stringResource(R.string.addEdit_label_notes), onClick = { showNotes = true })
            }
            if (!showPronunciation) {
                AddFieldChip(
                    label = stringResource(R.string.addEdit_label_irregular_pronunciation),
                    onClick = { showPronunciation = true },
                )
            }
        }
    }
}

@Composable
private fun RemovableTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    removeContentDescription: String,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        minLines = minLines,
        label = { Text(label) },
        trailingIcon = {
            IconButton(
                icon = SvenskaIcons.Close,
                contentDescription = removeContentDescription,
                onClick = onRemove,
            )
        },
    )
}

@Composable
private fun AddFieldChip(label: String, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = SvenskaIcons.Add,
                contentDescription = null,
                tint = SvenskaTheme.colors.primary,
                modifier = Modifier.size(18.dp),
            )
        },
    )
}
