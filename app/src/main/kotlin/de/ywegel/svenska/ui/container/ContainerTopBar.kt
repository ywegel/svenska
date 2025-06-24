@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.container

import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.common.IconButton
import de.ywegel.svenska.ui.theme.SvenskaIcons

@Composable
fun ContainerTopBar(
    scrollBehavior: TopAppBarScrollBehavior,
    editingContainers: Boolean,
    toggleIsEditMode: (Boolean) -> Unit,
    navigateToSettings: () -> Unit,
    navigateToSearch: () -> Unit,
    toggleEditMode: () -> Unit,
) {
    if (editingContainers) {
        TopAppBar(
            title = { Text(stringResource(R.string.container_title_edit_mode)) },
            navigationIcon = { IconButton(SvenskaIcons.Close, null) { toggleIsEditMode(false) } },
            scrollBehavior = scrollBehavior,
        )
    } else {
        TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            actions = {
                IconButton(
                    icon = SvenskaIcons.Edit,
                    contentDescription = null,
                    onClick = toggleEditMode,
                )
                IconButton(
                    icon = SvenskaIcons.Search,
                    contentDescription = null,
                    onClick = navigateToSearch,
                )
                IconButton(
                    icon = SvenskaIcons.Settings,
                    contentDescription = null,
                    onClick = navigateToSettings,
                )
            },
            scrollBehavior = scrollBehavior,
        )
    }
}
