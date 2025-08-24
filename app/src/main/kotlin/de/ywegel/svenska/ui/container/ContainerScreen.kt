@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.container

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Abc
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.FavoritesAndPronunciationScreenDestination
import com.ramcosta.composedestinations.generated.destinations.OverviewScreenDestination
import com.ramcosta.composedestinations.generated.destinations.QuizConfigurationScreenDestinationNavArgs
import com.ramcosta.composedestinations.generated.destinations.SearchScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsScreenDestination
import com.ramcosta.composedestinations.generated.destinations.SwedishNumbersScreenDestination
import com.ramcosta.composedestinations.generated.destinations.WordGroupsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import de.ywegel.svenska.R
import de.ywegel.svenska.data.model.GeneratorConstants
import de.ywegel.svenska.data.model.VocabularyContainer
import de.ywegel.svenska.data.model.container
import de.ywegel.svenska.data.model.containers
import de.ywegel.svenska.navigation.SvenskaGraph
import de.ywegel.svenska.ui.common.ConfirmButton
import de.ywegel.svenska.ui.common.ConfirmableComponent
import de.ywegel.svenska.ui.common.DismissButton
import de.ywegel.svenska.ui.common.HorizontalSpacerM
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.common.VerticalSpacerXS
import de.ywegel.svenska.ui.common.VerticalSpacerXXS
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Destination<SvenskaGraph>(start = true)
@Composable
fun ContainerScreen(navigator: DestinationsNavigator) {
    val viewModel: ContainerViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ContainerScreen(
        uiState = uiState,
        onContainerClick = {
            navigator.navigate(OverviewScreenDestination(it.id, it.name))
        },
        toggleIsEditMode = viewModel::updateIsEditMode,
        onDeleteClick = viewModel::deleteContainer,
        onAddEditContainer = viewModel::addEditContainer,
        onBonusClick = {
            when (it) {
                BonusScreen.Numbers -> navigator.navigate(SwedishNumbersScreenDestination)
                BonusScreen.Favorites -> navigator.navigate(
                    FavoritesAndPronunciationScreenDestination(it),
                )

                BonusScreen.SpecialPronunciation -> navigator.navigate(
                    FavoritesAndPronunciationScreenDestination(it),
                )

                BonusScreen.Quiz ->
                    navigator.navigate(NavGraphs.quiz(QuizConfigurationScreenDestinationNavArgs(containerId = null)))

                BonusScreen.WordGroups ->
                    navigator.navigate(WordGroupsScreenDestination)
            }
        },
        onSettingsClicked = { navigator.navigate(SettingsScreenDestination) },
        onSearchClicked = { navigator.navigate(SearchScreenDestination(containerId = null)) },
    )
}

@Composable
private fun ContainerScreen(
    uiState: ContainerUiState,
    onContainerClick: (VocabularyContainer) -> Unit,
    toggleIsEditMode: (Boolean) -> Unit,
    onDeleteClick: (VocabularyContainer) -> Unit,
    onAddEditContainer: (String, Int?) -> Unit,
    onBonusClick: (BonusScreen) -> Unit,
    onSettingsClicked: () -> Unit,
    onSearchClicked: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    BackHandler(uiState.isEditModeMode) {
        toggleIsEditMode(false)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ContainerTopBar(
                scrollBehavior = scrollBehavior,
                editingContainers = uiState.isEditModeMode,
                toggleIsEditMode = toggleIsEditMode,
                navigateToSettings = onSettingsClicked,
                navigateToSearch = onSearchClicked,
                toggleEditMode = { toggleIsEditMode(true) },
            )
        },
        floatingActionButton = {
            AddContainerButtonWithTextDialog(onConfirm = onAddEditContainer, existingContainerId = null) { onClick ->
                FloatingActionButton(onClick) {
                    Icon(SvenskaIcons.Add, null)
                }
            }
        },
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .consumeWindowInsets(contentPadding)
                .padding(horizontal = Spacings.m),
            contentPadding = contentPadding,
            verticalArrangement = Arrangement.spacedBy(Spacings.m),
        ) {
            items(uiState.containers, key = { it.id }) { container ->
                ContainerItem(
                    container = container,
                    isEditMode = uiState.isEditModeMode,
                    onClick = onContainerClick,
                    toggleIsEditMode = toggleIsEditMode,
                    onDeleteClick = onDeleteClick,
                    updateContainerName = onAddEditContainer,
                )
            }
            item {
                ContainerActionsFooter(onBonusClick = onBonusClick)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ContainerActionsFooter(onBonusClick: (BonusScreen) -> Unit) {
    // TODO: contentDescription
    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        BonusScreen.entries.forEach { bonus ->
            FooterActionButton(
                title = bonus.userFacingTitle(),
                icon = bonus.userFacingIcon(),
                onClick = { onBonusClick(bonus) },
            )
        }
    }
}

@Composable
private fun FooterActionButton(title: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(Spacings.xs),
    ) {
        FilledIconButton(
            onClick = onClick,
            shape = SvenskaTheme.shapes.medium,
        ) {
            Icon(icon, null)
        }
        VerticalSpacerXXS()
        Text(title)
    }
}

enum class BonusScreen {
    Numbers,
    Favorites,
    SpecialPronunciation,
    Quiz,
    WordGroups,
    ;

    @Composable
    fun userFacingTitle(): String {
        return when (this) {
            Numbers -> stringResource(R.string.containers_bonus_numbers)
            Favorites -> stringResource(R.string.containers_bonus_favorites)
            SpecialPronunciation -> stringResource(R.string.containers_bonus_special_pronunciation)
            Quiz -> stringResource(R.string.containers_bonus_quiz)
            WordGroups -> stringResource(R.string.containers_bonus_word_groups)
        }
    }

    @ReadOnlyComposable
    fun userFacingIcon(): ImageVector {
        return when (this) {
            Numbers -> SvenskaIcons.Pin
            Favorites -> SvenskaIcons.Favorite
            SpecialPronunciation -> SvenskaIcons.Warning
            Quiz -> SvenskaIcons.Quiz
            WordGroups -> SvenskaIcons.Abc
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ContainerItem(
    container: VocabularyContainer,
    isEditMode: Boolean = false,
    onClick: (VocabularyContainer) -> Unit,
    toggleIsEditMode: (Boolean) -> Unit,
    onDeleteClick: (VocabularyContainer) -> Unit,
    updateContainerName: (String, Int?) -> Unit,
) {
    Card(
        modifier = Modifier.combinedClickable(
            onLongClick = { toggleIsEditMode(true) },
            onClick = { onClick(container) },
        ),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = Spacings.l),
        ) {
            Row(
                Modifier
                    .padding(vertical = Spacings.xl)
                    .weight(1f),
            ) {
                Text(text = "${container.id}.")
                HorizontalSpacerM()
                Text(
                    text = container.name,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (isEditMode) {
                AddContainerButtonWithTextDialog(
                    initialInput = container.name,
                    onConfirm = updateContainerName,
                    existingContainerId = container.id,
                ) { onClick ->
                    IconButton(
                        icon = SvenskaIcons.Edit,
                        contentDescription = null,
                        onClick = onClick,
                    )
                }
                ConfirmableComponent(
                    dialogTitle = "Delete Container?",
                    dialogText = "You will permanently delete this container with all words associated to it!",
                    onConfirm = { onDeleteClick(container) },
                ) { click ->
                    IconButton(SvenskaIcons.DeleteForever, null, onClick = click)
                }
            }
        }
    }
}

@Composable
private fun AddContainerButtonWithTextDialog(
    initialInput: String = "",
    existingContainerId: Int?,
    onConfirm: (String, Int?) -> Unit,
    component: @Composable (onClick: () -> Unit) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var inputText by remember { mutableStateOf(initialInput) }

    component { showDialog = true }

    BackHandler(showDialog) {
        showDialog = false
        inputText = ""
    }

    val dialogTitle = if (inputText.isEmpty()) {
        stringResource(R.string.containers_add_title)
    } else {
        stringResource(R.string.containers_edit_title)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(dialogTitle) },
            text = {
                Column {
                    VerticalSpacerXS()
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text(stringResource(R.string.containers_add_input_label)) },
                        isError = inputText.isBlank(),
                        supportingText = {
                            if (inputText.isBlank()) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = stringResource(R.string.containers_add_input_error),
                                    color = SvenskaTheme.colors.error,
                                )
                            }
                        },
                    )
                }
            },
            confirmButton = {
                ConfirmButton(stringResource(R.string.containers_add_confirmation), enabled = inputText.isNotBlank()) {
                    if (inputText.isNotBlank()) {
                        showDialog = false
                        onConfirm(inputText, existingContainerId)
                        inputText = ""
                    }
                }
            },
            dismissButton = {
                DismissButton {
                    showDialog = false
                    inputText = ""
                }
            },
        )
    }
}

@Preview
@Composable
private fun ContainerCardPreview() {
    SvenskaTheme {
        ContainerItem(
            container(),
            isEditMode = false,
            onClick = {},
            toggleIsEditMode = {},
            onDeleteClick = {},
            updateContainerName = { a, b -> },
        )
    }
}

@Preview
@Composable
private fun ContainerCardEditModePreview() {
    SvenskaTheme {
        ContainerItem(
            container(),
            isEditMode = true,
            onClick = {},
            toggleIsEditMode = {},
            onDeleteClick = {},
            updateContainerName = { a, b -> },
        )
    }
}

@Preview
@Composable
private fun ContainerCardEditModeLongTextPreview() {
    SvenskaTheme {
        val container = VocabularyContainer(
            name = GeneratorConstants.LONG_STRING,
        )
        ContainerItem(
            container = container,
            isEditMode = true,
            onClick = {},
            toggleIsEditMode = {},
            onDeleteClick = {},
            updateContainerName = { a, b -> },
        )
    }
}

@Preview
@Composable
private fun ContainerPreview() {
    SvenskaTheme {
        ContainerScreen(
            ContainerUiState(containers = containers()),
            onContainerClick = {},
            toggleIsEditMode = {},
            onDeleteClick = {},
            onAddEditContainer = { a, b -> },
            onBonusClick = {},
            onSettingsClicked = {},
            onSearchClicked = {},
        )
    }
}
