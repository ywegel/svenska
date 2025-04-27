package de.ywegel.svenska.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Spacings {
    val xxxs: Dp = 2.dp
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val s: Dp = 12.dp
    val m: Dp = 16.dp
    val l: Dp = 20.dp
    val xl: Dp = 24.dp
    val xxl: Dp = 32.dp
    val xxxl: Dp = 44.dp
    val xxxxl: Dp = 60.dp
}

object UiConstants {
    val columnHeaderContentPadding = PaddingValues(vertical = Spacings.xs, horizontal = Spacings.m)
    val columnWithHeaderTopPadding = Spacings.xs
}
