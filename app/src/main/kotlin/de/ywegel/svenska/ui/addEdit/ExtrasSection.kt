package de.ywegel.svenska.ui.addEdit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.outlined.Add
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
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

/** Rarely used fields. They only take space once they have content or the user adds them. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExtrasSection(uiState: AddEditUiState, callbacks: AddEditVocabularyCallbacks, modifier: Modifier = Modifier) {
    var showNotes by rememberSaveable { mutableStateOf(uiState.notes.isNotBlank()) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(Spacings.m)) {
        if (showNotes) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.notes,
                onValueChange = callbacks::updateNotes,
                minLines = 2,
                label = { Text(stringResource(R.string.addEdit_label_notes)) },
            )
        }
        if (uiState.isIrregularPronunciation) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.irregularPronunciation.orEmpty(),
                onValueChange = callbacks::updateIrregularPronunciation,
                label = { Text(stringResource(R.string.addEdit_label_irregular_pronunciation)) },
            )
        }

        FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacings.xs)) {
            if (!showNotes) {
                AddFieldChip(label = stringResource(R.string.addEdit_label_notes), onClick = { showNotes = true })
            }
            if (!uiState.isIrregularPronunciation) {
                AddFieldChip(
                    label = stringResource(R.string.addEdit_label_irregular_pronunciation),
                    onClick = { callbacks.updateIsIrregularPronunciation(true) },
                )
            }
        }
    }
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
