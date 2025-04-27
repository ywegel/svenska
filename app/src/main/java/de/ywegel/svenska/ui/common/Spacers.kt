@file:Suppress("complexity.TooManyFunctions")
package de.ywegel.svenska.ui.common

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import de.ywegel.svenska.ui.theme.Spacings

@Composable
private fun VerticalSpacer(spacing: Dp) {
    Spacer(modifier = Modifier.requiredHeight(spacing))
}

@Composable
fun VerticalSpacerXXS() {
    VerticalSpacer(spacing = Spacings.xxs)
}

@Composable
fun VerticalSpacerXS() {
    VerticalSpacer(spacing = Spacings.xs)
}

@Composable
fun VerticalSpacerS() {
    VerticalSpacer(spacing = Spacings.s)
}

@Composable
fun VerticalSpacerM() {
    VerticalSpacer(spacing = Spacings.m)
}

@Composable
fun VerticalSpacerL() {
    VerticalSpacer(spacing = Spacings.l)
}

@Composable
fun VerticalSpacerXL() {
    VerticalSpacer(spacing = Spacings.xl)
}

@Composable
fun VerticalSpacerXXXXL() {
    VerticalSpacer(spacing = Spacings.xxxxl)
}

@Composable
private fun HorizontalSpacer(spacing: Dp) {
    Spacer(modifier = Modifier.requiredWidth(spacing))
}

@Composable
fun HorizontalSpacerXS() {
    HorizontalSpacer(spacing = Spacings.xs)
}

@Composable
fun HorizontalSpacerS() {
    HorizontalSpacer(spacing = Spacings.s)
}

@Composable
fun HorizontalSpacerM() {
    HorizontalSpacer(spacing = Spacings.m)
}

@Composable
fun HorizontalSpacerL() {
    HorizontalSpacer(spacing = Spacings.l)
}

@Composable
fun HorizontalSpacerXXL() {
    HorizontalSpacer(spacing = Spacings.xxl)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NavigationBarSpacer() {
    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBarsIgnoringVisibility))
}
