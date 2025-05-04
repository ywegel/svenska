package de.ywegel.svenska.ui.common

import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.theme.SvenskaIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppTextBar(
    title: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector = SvenskaIcons.Close,
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            NavigationIconButton(
                onNavigateUp = onNavigateUp,
                navigationIcon = navigationIcon,
            )
        },
    )
}

@Composable
fun NavigationIconButton(onNavigateUp: () -> Unit, navigationIcon: ImageVector = SvenskaIcons.Close) {
    IconButton(onClick = onNavigateUp) {
        Icon(
            imageVector = navigationIcon,
            contentDescription = stringResource(R.string.accessibility_toolbar_navigate_up),
        )
    }
}
