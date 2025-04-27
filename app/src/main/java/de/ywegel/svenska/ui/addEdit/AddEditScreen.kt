@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.addEdit

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.ui.common.ConfirmableComponent
import de.ywegel.svenska.ui.common.HorizontalSpacerXS
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.overview.userFacingString
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination(navArgsDelegate = AddEditNavArgs::class)
@Composable
fun AddEditScreen(navigator: DestinationsNavigator) {
    val viewModel: AddEditViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()

    AddEditScreen(
        uiState = uiState,
        updateSelectedWordGroup = viewModel::updateSelectedWordGroup,
        updateGender = viewModel::updateGender,
        updateWordWithAnnotation = viewModel::updateWordWithAnnotation,
        updateTranslation = viewModel::updateTranslation,
        updateEnding = viewModel::updateEnding,
        updateNotes = viewModel::updateNotes,
        discardAndNavigateUp = navigator::navigateUp,
        saveAndNavigateUp = { viewModel.saveAndGoBack(navigator::navigateUp) },
        updateIsFavorite = viewModel::updateIsFavorite,
        updateIrregularPronunciation = viewModel::updateIsIrregularPronunciation,
        deleteItem = { viewModel.deleteVocabulary(navigator::navigateUp) },
    )
}

@Composable
private fun AddEditScreen(
    uiState: UiState,
    updateSelectedWordGroup: (WordGroup) -> Unit = {},
    updateGender: (Gender) -> Unit = {},
    updateWordWithAnnotation: (String) -> Unit = {},
    updateTranslation: (String) -> Unit = {},
    updateEnding: (String) -> Unit = {},
    updateNotes: (String) -> Unit = {},
    discardAndNavigateUp: () -> Unit = {},
    saveAndNavigateUp: () -> Unit = {},
    updateIsFavorite: (Boolean) -> Unit = {},
    updateIrregularPronunciation: (Boolean) -> Unit = {},
    deleteItem: () -> Unit = {},
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                translation = uiState.translation,
                editingExisting = uiState.editingExistingVocabulary,
                isFavorite = uiState.isFavorite,
                scrollBehavior = scrollBehavior,
                navigateUp = discardAndNavigateUp,
                updateIsFavorite = updateIsFavorite,
                deleteItem = deleteItem,
            )
        },
        floatingActionButton = {
            FloatingActionButton(saveAndNavigateUp) {
                Icon(imageVector = SvenskaIcons.Done, contentDescription = null)
            }
        },
    ) { contentPadding ->
        Column(
            Modifier
                .padding(contentPadding)
                .padding(horizontal = Spacings.m)
                .fillMaxWidth()
                .scrollable(rememberScrollState(), Orientation.Vertical),
        ) {
            WordGroupSelection(
                uiState.selectedWordGroup,
                onSelectionChanged = updateSelectedWordGroup,
            )
            VerticalSpacerM()
            Row {
                if (uiState.selectedWordGroup is WordGroup.Noun) {
                    GenderDropDown(
                        selectedGender = uiState.gender ?: Gender.defaultIfEmpty,
                        onGenderSelected = updateGender,
                    )
                    HorizontalSpacerXS()
                }
                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    value = uiState.wordWithAnnotation,
                    onValueChange = updateWordWithAnnotation,
                    label = { Text(stringResource(R.string.addEdit_label_word)) },
                )
            }
            VerticalSpacerM()
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.translation,
                onValueChange = updateTranslation,
                label = { Text(stringResource(R.string.addEdit_label_translation)) },
            )
            VerticalSpacerM()
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.ending,
                onValueChange = updateEnding,
                label = { Text(stringResource(R.string.addEdit_label_endings)) },
            )
            VerticalSpacerM()
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.notes,
                onValueChange = updateNotes,
                minLines = 2,
                label = { Text(stringResource(R.string.addEdit_label_notes)) },
            )
            VerticalSpacerM()
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    uiState.isIrregularPronunciation,
                    onCheckedChange = updateIrregularPronunciation,
                )
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = uiState.irregularPronunciation.orEmpty(),
                    enabled = uiState.isIrregularPronunciation,
                    onValueChange = updateNotes,
                    label = { Text(stringResource(R.string.addEdit_label_irregular_pronunciation)) },
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    translation: String,
    editingExisting: String?,
    isFavorite: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateUp: () -> Unit,
    updateIsFavorite: (Boolean) -> Unit = {},
    deleteItem: () -> Unit = {},
) {
    val title = editingExisting?.let { stringResource(R.string.addEdit_title_existing, it) }
        ?: stringResource(R.string.addEdit_title_new)

    TopAppBar(
        title = { Text(text = title) },
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = navigateUp) {
                Icon(imageVector = SvenskaIcons.Close, contentDescription = null)
            }
        },
        actions = {
            IconToggleButton(checked = isFavorite, onCheckedChange = updateIsFavorite) {
                Icon(if (isFavorite) SvenskaIcons.Favorite else SvenskaIcons.FavoriteBorder, null)
            }
            editingExisting?.let {
                ConfirmableComponent(
                    dialogTitle = stringResource(R.string.addEdit_confirm_delete_title),
                    dialogText = stringResource(R.string.addEdit_confirm_delete_body, translation),
                    onConfirm = deleteItem,
                ) { onClick ->
                    IconButton(onClick = onClick) {
                        Icon(SvenskaIcons.Delete, null)
                    }
                }
            }
        },
    )
}

@Composable
private fun WordGroupSelection(selected: WordGroup, onSelectionChanged: (WordGroup) -> Unit) {
    SingleChoiceSegmentedButtonRow {
        WordGroup.abstractWordGroups.forEachIndexed { index, group ->
            SegmentedButton(
                selected = selected == group,
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = WordGroup.abstractWordGroups.size,
                ),
                onClick = { onSelectionChanged(group) },
            ) {
                Text(text = stringResource(group.userFacingString()))
            }
        }
    }
}

@Composable
private fun GenderDropDown(selectedGender: Gender, onGenderSelected: (Gender) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded,
        onExpandedChange = { expanded = it },
    ) {
        /*
                OutlinedTextField(
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .menuAnchor(MenuAnchorType.PrimaryEditable),
            value = stringResource(selectedGender.userFacingString()),
            readOnly = true,
            onValueChange = {},
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded,
                    modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable),
                )
            },
        )
         */

        // Equivalent to the upper OutlinedTextField. Needed, as OutlinedTextField has a default width, that you can not remove.
        // TODO: Replace with a working OutlinedTextField, that has no fixed min width
        val value = stringResource(selectedGender.userFacingString())
        val interactionSource = remember { MutableInteractionSource() }
        CompositionLocalProvider(
            LocalTextSelectionColors provides OutlinedTextFieldDefaults.colors().textSelectionColors,
        ) {
            BasicTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.width(IntrinsicSize.Min),
                decorationBox =
                @Composable { innerTextField ->
                    OutlinedTextFieldDefaults.DecorationBox(
                        value = value,
                        innerTextField = innerTextField,
                        enabled = true,
                        singleLine = true,
                        visualTransformation = VisualTransformation.None,
                        interactionSource = interactionSource,
                        container = {
                            OutlinedTextFieldDefaults.Container(
                                enabled = true,
                                isError = false,
                                interactionSource = interactionSource,
                                colors = OutlinedTextFieldDefaults.colors(),
                                shape = OutlinedTextFieldDefaults.shape,
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = expanded,
                                modifier = Modifier.menuAnchor(MenuAnchorType.SecondaryEditable),
                            )
                        },
                    )
                },
            )
        }
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Gender.entries.forEach {
                DropdownMenuItem(
                    text = { Text(stringResource(it.userFacingString())) },
                    onClick = {
                        onGenderSelected(it)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

data class AddEditNavArgs(
    val containerId: Int,
    val initialVocabulary: Vocabulary? = null,
)

@Preview
@Composable
private fun AddEditScreenPreview() {
    SvenskaTheme {
        AddEditScreen(uiState = UiState())
    }
}
