@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.addEdit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.destinations.WordGroupsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.Gender
import de.ywegel.svenska.data.model.Vocabulary
import de.ywegel.svenska.data.model.WordGroup
import de.ywegel.svenska.navigation.SvenskaGraph
import de.ywegel.svenska.navigation.transitions.LateralTransition
import de.ywegel.svenska.navigation.transitions.TemporaryHierarchicalTransitionStyle
import de.ywegel.svenska.ui.addEdit.models.ViewWordGroup
import de.ywegel.svenska.ui.addEdit.models.ViewWordSubGroup
import de.ywegel.svenska.ui.addEdit.models.mainGroupAbbreviation
import de.ywegel.svenska.ui.addEdit.models.subGroupAbbreviation
import de.ywegel.svenska.ui.common.ConfirmableComponent
import de.ywegel.svenska.ui.common.HorizontalSpacerXS
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.common.VerticalSpacerM
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.common.vocabulary.HighlightUtils
import de.ywegel.svenska.ui.common.vocabulary.wordGroupBadge.AnimatedWordGroupBadgeExtended
import de.ywegel.svenska.ui.common.vocabulary.wordGroupBadge.EmptyWordGroupBadge
import de.ywegel.svenska.ui.overview.userFacingString
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme
import de.ywegel.svenska.ui.theme.Typography
import kotlinx.coroutines.flow.collectLatest

@Destination<SvenskaGraph>(
    navArgs = AddEditNavArgs::class,
    style = TemporaryHierarchicalTransitionStyle::class,
)
@Composable
fun EditVocabularyScreen(navigator: DestinationsNavigator) {
    AddEditScreen(navigator)
}

@Destination<SvenskaGraph>(
    navArgs = AddEditNavArgs::class,
    style = LateralTransition::class,
)
@Composable
fun AddVocabularyScreen(navigator: DestinationsNavigator) {
    AddEditScreen(navigator)
}

@Composable
private fun AddEditScreen(navigator: DestinationsNavigator) {
    val viewModel: AddEditViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner.lifecycle) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.uiEvents.collectLatest { event ->
                when (event) {
                    AddEditViewModel.UiEvent.NavigateUp -> navigator.navigateUp()
                    AddEditViewModel.UiEvent.InvalidWordGroupConfiguration -> {
                        snackbarHostState.showSnackbar(
                            message = context.getString(R.string.addEdit_group_selector_invalid_configuration),
                        )
                    }
                }
            }
        }
    }

    AddEditScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        callbacks = viewModel,
        navigateUp = navigator::navigateUp,
        navigateToWordGroupsScreen = { navigator.navigate(WordGroupsScreenDestination) },
    )
}

@Composable
private fun AddEditScreen(
    uiState: AddEditUiState,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    callbacks: AddEditVocabularyCallbacks,
    navigateUp: () -> Unit,
    navigateToWordGroupsScreen: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                translation = uiState.translation,
                editingExisting = uiState.editingExistingVocabulary,
                isFavorite = uiState.isFavorite,
                scrollBehavior = scrollBehavior,
                navigateUp = navigateUp,
                updateIsFavorite = callbacks::updateIsFavorite,
                deleteItem = { callbacks.deleteVocabulary() },
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton({ callbacks.saveAndNavigateUp() }) {
                Icon(imageVector = SvenskaIcons.Done, contentDescription = null)
            }
        },
    ) { contentPadding ->
        Column(
            Modifier
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .fillMaxSize()
                .padding(contentPadding)
                .imePadding()
                .verticalScroll(rememberScrollState()),
        ) {
            WordGroupSection(
                uiState = uiState,
                callbacks = callbacks,
                navigateToWordGroupsScreen = navigateToWordGroupsScreen,
            )

            VerticalSpacerM()

            AddEditInputSection(uiState = uiState, callbacks = callbacks)
        }
    }
}

@Composable
private fun WordGroupSection(
    uiState: AddEditUiState,
    callbacks: AddEditVocabularyCallbacks,
    navigateToWordGroupsScreen: () -> Unit,
) {
    // TODO: If the user selects something and has no ending set, we want to show a button which lets him apply the default endings for the wordgroup
    WordGroupSelection(
        selectedGroup = uiState.selectedWordGroup,
        selectedSubgroup = uiState.selectedSubGroup,
        onGroupSelected = callbacks::updateSelectedWordGroup,
        onSubgroupSelected = callbacks::updateSelectedSubWordGroup,
    )

    Row(Modifier.padding(horizontal = Spacings.m), verticalAlignment = Alignment.CenterVertically) {
        Text("Selected word group:")
        HorizontalSpacerXS()
        Crossfade(uiState.selectedWordGroup) { mainGroup ->
            if (mainGroup != null) {
                AnimatedWordGroupBadgeExtended(
                    mainWordGroup = mainGroup.mainGroupAbbreviation(),
                    subWordGroup = mainGroup.subGroupAbbreviation(uiState.selectedSubGroup),
                )
            } else {
                EmptyWordGroupBadge()
            }
        }
        Spacer(Modifier.weight(1f))
        IconButton(
            icon = SvenskaIcons.Info,
            contentDescription = stringResource(R.string.accessibility_addEdit_navigate_word_group_info_screen),
            onClick = navigateToWordGroupsScreen,
        )
    }
}

@Suppress("LongMethod")
@Composable
private fun AddEditInputSection(uiState: AddEditUiState, callbacks: AddEditVocabularyCallbacks) {
    val optionalHighlightedWord: AnnotatedString? = remember(uiState.wordWithAnnotation) {
        return@remember if (uiState.wordWithAnnotation.contains('*')) {
            HighlightUtils.buildAnnotatedWord(uiState.wordWithAnnotation)
        } else {
            null
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = Spacings.m)
            .fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            if (uiState.selectedWordGroup == ViewWordGroup.Noun) {
                GenderDropDown(
                    selectedGender = uiState.gender ?: Gender.defaultIfEmpty,
                    onGenderSelected = callbacks::updateGender,
                )
                HorizontalSpacerXS()
            }
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                value = uiState.wordWithAnnotation,
                onValueChange = callbacks::updateWordWithAnnotation,
                label = { Text(stringResource(R.string.addEdit_label_word)) },
            )
        }
        VerticalSpacerXS()
        optionalHighlightedWord?.let {
            Text(text = it)
        }
        AnimatedVisibility(!uiState.annotationInformationHidden) {
            AnnotationInformation(callbacks::hideAnnotationInfo)
        }
        VerticalSpacerXS()
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.translation,
            onValueChange = callbacks::updateTranslation,
            label = { Text(stringResource(R.string.addEdit_label_translation)) },
        )
        VerticalSpacerM()
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.ending,
            onValueChange = callbacks::updateEnding,
            label = { Text(stringResource(R.string.addEdit_label_endings)) },
        )
        // TODO: After the user entered his endings, we want to suggest a word sub-group, which the user can apply by clicking a button
        VerticalSpacerM()
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = uiState.notes,
            onValueChange = callbacks::updateNotes,
            minLines = 2,
            label = { Text(stringResource(R.string.addEdit_label_notes)) },
        )
        VerticalSpacerM()
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                uiState.isIrregularPronunciation,
                onCheckedChange = callbacks::updateIsIrregularPronunciation,
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = uiState.irregularPronunciation.orEmpty(),
                enabled = uiState.isIrregularPronunciation,
                onValueChange = callbacks::updateIrregularPronunciation,
                label = { Text(stringResource(R.string.addEdit_label_irregular_pronunciation)) },
            )
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
            IconButton(icon = SvenskaIcons.Close, contentDescription = null, onClick = navigateUp)
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
                    IconButton(
                        icon = SvenskaIcons.Delete,
                        contentDescription = null,
                        onClick = onClick,
                    )
                }
            }
        },
    )
}

@Suppress("LongMethod")
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
        val value = stringResource(selectedGender.userFacingString())
        val interactionSource = remember { MutableInteractionSource() }
        CompositionLocalProvider(
            LocalTextSelectionColors provides OutlinedTextFieldDefaults.colors().textSelectionColors,
        ) {
            BasicTextField(
                value = value,
                onValueChange = {},
                textStyle = SvenskaTheme.typography.bodyLarge,
                readOnly = true,
                modifier = Modifier.width(IntrinsicSize.Min),
                decorationBox = @Composable { innerTextField ->
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
                    text = {
                        Text(
                            text = stringResource(it.userFacingString()),
                            style = SvenskaTheme.typography.bodyLarge,
                        )
                    },
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

@Composable
private fun AnnotationInformation(hideAnnotationInfo: () -> Unit) {
    Card {
        Row(Modifier.padding(horizontal = Spacings.m, vertical = Spacings.xs), verticalAlignment = Alignment.CenterVertically) {
            Text(
                modifier = Modifier.weight(1f),
                text = buildAnnotatedString {
                    append(stringResource(R.string.addEdit_annotation_information_text))
                    append(' ')
                    @Suppress("MagicNumber")
                    append(HighlightUtils.buildAnnotatedWord("eller", listOf(1 to 3)))
                },
                style = Typography.bodyMedium,
            )
            IconButton(
                SvenskaIcons.Close,
                contentDescription = stringResource(R.string.accessibility_addEdit_annotation_information_hide),
                onClick = hideAnnotationInfo,
            )
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
        AddEditScreen(
            uiState = AddEditUiState(),
            callbacks = AddEditVocabularyCallbacksFake,
            navigateUp = {},
            navigateToWordGroupsScreen = {},
        )
    }
}

@Preview
@Composable
private fun AddEditScreenWithAnnotatedWordPreview() {
    SvenskaTheme {
        AddEditScreen(
            uiState = AddEditUiState(
                wordWithAnnotation = "t*e*st*With*An**not*at*ions",
            ),
            callbacks = AddEditVocabularyCallbacksFake,
            navigateUp = {},
            navigateToWordGroupsScreen = {},
        )
    }
}

@Preview
@Composable
private fun AddEditScreenSelectionExpandedPreview() {
    SvenskaTheme {
        AddEditScreen(
            uiState = AddEditUiState(
                selectedWordGroup = ViewWordGroup.Noun,
                selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.OR),
                annotationInformationHidden = false,
            ),
            callbacks = AddEditVocabularyCallbacksFake,
            navigateUp = {},
            navigateToWordGroupsScreen = {},
        )
    }
}
