package de.ywegel.svenska.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Calculates and remembers padding values for a Column inside a Scaffold.
 * Use this to ensure the Column respects top, start, and end insets while allowing
 * content to be drawn behind the navigation bar.
 * Add a [de.ywegel.svenska.ui.common.NavigationBarSpacer] at the end of the Column to handle bottom insets.
 */
@Composable
fun rememberColumnScaffoldInsets(paddingValues: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return remember(paddingValues, layoutDirection) {
        PaddingValues(
            top = paddingValues.calculateTopPadding(),
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            // 0 to allow drawing behind the navbar. A spacer is added at the end of the Column
            bottom = 0.dp,
        )
    }
}
