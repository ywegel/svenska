@file:OptIn(ExperimentalMaterial3Api::class)

package de.ywegel.svenska.ui.common

import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import de.ywegel.svenska.R
import de.ywegel.svenska.ui.theme.Spacings
import de.ywegel.svenska.ui.theme.SvenskaIcons
import de.ywegel.svenska.ui.theme.SvenskaTheme

@Composable
fun TopAppTextBar(
    title: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigationIcon: ImageVector = SvenskaIcons.Close,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            NavigationIconButton(
                onNavigateUp = onNavigateUp,
                navigationIcon = navigationIcon,
            )
        },
        actions = actions,
    )
}

@Composable
fun ProgressToolbar(
    progress: Int?,
    progressGoal: Int?,
    fallBackTitle: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigationIcon: ImageVector = SvenskaIcons.Close,
) {
    val cleanedProgress by remember(progress, progressGoal) {
        derivedStateOf { progressGoal?.let { progress?.toFloat()?.div(it) } ?: 0f }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = cleanedProgress,
        label = "progress animation",
        animationSpec = tween(delayMillis = 50, easing = EaseOutBack),
    )
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (progress != null) {
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = "$progress /\n$progressGoal",
                        modifier = Modifier.padding(start = Spacings.m, end = Spacings.xxs),
                        style = SvenskaTheme.typography.bodyMedium,
                    )
                } else {
                    Text(text = fallBackTitle)
                }
            }
        },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
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
            contentDescription = stringResource(R.string.accessibility_general_toolbar_navigate_up),
        )
    }
}

@Preview
@Composable
private fun ProgressToolbarPreview() {
    SvenskaTheme {
        ProgressToolbar(
            progress = 40,
            progressGoal = 100,
            fallBackTitle = "FallbackTitle",
            onNavigateUp = {},
        )
    }
}
