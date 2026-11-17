@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.addEdit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import de.ywegel.svenska.ui.common.ConfirmableComponent
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.overview.VocabularyListItem
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme
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
    Scaffold(
        topBar = {
            Header(
                uiState = uiState,
                navigateUp = navigateUp,
                updateIsFavorite = callbacks::updateIsFavorite,
                deleteItem = callbacks::deleteVocabulary,
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = callbacks::saveAndNavigateUp) {
                Icon(
                    imageVector = SvenskaIcons.Done,
                    contentDescription = stringResource(R.string.accessibility_addEdit_fab_done),
                )
            }
        },
    ) { contentPadding ->
        AddEditContent(
            uiState = uiState,
            callbacks = callbacks,
            navigateToWordGroupsScreen = navigateToWordGroupsScreen,
            modifier = Modifier.padding(contentPadding),
        )
    }

    if (uiState.showSwitchToEndingsDialog) {
        SwitchToEndingsDialog(
            onConfirm = callbacks::confirmSwitchToEndings,
            onDismiss = callbacks::dismissSwitchToEndings,
        )
    }
}

@Composable
private fun AddEditContent(
    uiState: AddEditUiState,
    callbacks: AddEditVocabularyCallbacks,
    navigateToWordGroupsScreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(top = Spacings.s),
            verticalArrangement = Arrangement.spacedBy(Spacings.m),
        ) {
            WordGroupSelection(
                selectedGroup = uiState.selectedWordGroup,
                selectedSubgroup = uiState.selectedSubGroup,
                onGroupSelected = callbacks::updateSelectedWordGroup,
                onSubgroupSelected = callbacks::updateSelectedSubWordGroup,
                trailingContent = {
                    IconButton(
                        icon = SvenskaIcons.Info,
                        contentDescription = stringResource(
                            R.string.accessibility_addEdit_navigate_word_group_info_screen,
                        ),
                        onClick = navigateToWordGroupsScreen,
                    )
                },
            )

            val sectionModifier = Modifier.padding(horizontal = Spacings.m)
            WordSection(uiState, callbacks, sectionModifier)
            OutlinedTextField(
                modifier = sectionModifier.fillMaxWidth(),
                value = uiState.translation,
                onValueChange = callbacks::updateTranslation,
                label = { Text(stringResource(R.string.addEdit_label_translation)) },
            )
            if (uiState.hasForms) {
                FormsSection(uiState, callbacks, sectionModifier)
            }
            ExtrasSection(uiState, callbacks, sectionModifier)

            // Keeps the last field above the floating action button
            Spacer(Modifier.height(FabClearance))
        }
}

private val FabClearance = Spacings.xxxxl + Spacings.xl

/** The app bar shows the vocabulary as the list will show it. */
@Composable
private fun Header(
    uiState: AddEditUiState,
    navigateUp: () -> Unit,
    updateIsFavorite: (Boolean) -> Unit,
    deleteItem: () -> Unit,
) {
    val previewVocabulary = remember(uiState) { uiState.toPreviewVocabulary() }

    Column(
        modifier = Modifier
            .background(
                color = SvenskaTheme.colors.surfaceContainer,
                shape = RoundedCornerShape(bottomStart = Spacings.xxl, bottomEnd = Spacings.xxl),
            )
            .padding(bottom = Spacings.m),
    ) {
        TopBar(
            translation = uiState.translation,
            editingExisting = uiState.editingExistingVocabulary,
            isFavorite = uiState.isFavorite,
            navigateUp = navigateUp,
            updateIsFavorite = updateIsFavorite,
            deleteItem = deleteItem,
        )
        VocabularyListItem(
            vocabulary = previewVocabulary,
            modifier = Modifier.padding(horizontal = Spacings.m),
            containerColor = SvenskaTheme.colors.surfaceContainerLowest,
        )
    }
}

@Composable
private fun TopBar(
    translation: String,
    editingExisting: String?,
    isFavorite: Boolean,
    navigateUp: () -> Unit,
    updateIsFavorite: (Boolean) -> Unit,
    deleteItem: () -> Unit,
) {
    val title = editingExisting?.let { stringResource(R.string.addEdit_title_existing, it) }
        ?: stringResource(R.string.addEdit_title_new)

    TopAppBar(
        title = { Text(text = title) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = SvenskaTheme.colors.surfaceContainer),
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

@Composable
private fun SwitchToEndingsDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.addEdit_forms_switch_dialog_title)) },
        text = { Text(stringResource(R.string.addEdit_forms_switch_dialog_body)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.addEdit_forms_switch_dialog_confirm)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.general_cancel)) }
        },
    )
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

@Preview(heightDp = 1000)
@Composable
private fun AddEditScreenEndingsPreview() {
    SvenskaTheme {
        AddEditScreen(
            uiState = AddEditUiState(
                selectedWordGroup = ViewWordGroup.Noun,
                selectedSubGroup = ViewWordSubGroup.Noun(WordGroup.NounSubgroup.AR),
                gender = Gender.Ultra,
                wordWithAnnotation = "högsta dr*ö*m",
                translation = "größter Traum",
                endings = "-men -mar -marna",
            ),
            callbacks = AddEditVocabularyCallbacksFake,
            navigateUp = {},
            navigateToWordGroupsScreen = {},
        )
    }
}

@Preview(heightDp = 1000)
@Composable
private fun AddEditScreenSuggestionAndReviewPreview() {
    SvenskaTheme {
        AddEditScreen(
            uiState = AddEditUiState(
                selectedWordGroup = ViewWordGroup.Noun,
                gender = Gender.Neutra,
                wordWithAnnotation = "exempel",
                translation = "Beispiel",
                endings = "-et, -, -en",
                editingExistingVocabulary = "exempel",
            ),
            callbacks = AddEditVocabularyCallbacksFake,
            navigateUp = {},
            navigateToWordGroupsScreen = {},
        )
    }
}

@Preview(heightDp = 1000)
@Composable
private fun AddEditScreenWrittenOutPreview() {
    SvenskaTheme {
        AddEditScreen(
            uiState = AddEditUiState(
                selectedWordGroup = ViewWordGroup.Verb,
                selectedSubGroup = ViewWordSubGroup.Verb(WordGroup.VerbSubgroup.GROUP_4_SPECIAL),
                wordWithAnnotation = "ta en dusch",
                translation = "duschen",
                formsMode = FormsMode.WrittenOut,
                writtenForms = listOf("tar", "tog", "tagit"),
                isFavorite = true,
                editingExistingVocabulary = "ta",
            ),
            callbacks = AddEditVocabularyCallbacksFake,
            navigateUp = {},
            navigateToWordGroupsScreen = {},
        )
    }
}

@Preview
@Composable
private fun AddEditScreenHighlightErrorPreview() {
    SvenskaTheme {
        AddEditScreen(
            uiState = AddEditUiState(
                selectedWordGroup = ViewWordGroup.Verb,
                wordWithAnnotation = "känna s*i*g",
            ),
            callbacks = AddEditVocabularyCallbacksFake,
            navigateUp = {},
            navigateToWordGroupsScreen = {},
        )
    }
}
