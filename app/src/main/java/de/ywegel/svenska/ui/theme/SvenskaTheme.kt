package de.ywegel.svenska.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

object SvenskaTheme {
    val colors: ColorScheme
        @ReadOnlyComposable
        @Composable
        get() = LocalSvenskaColors.current

    val typography: Typography
        @ReadOnlyComposable
        @Composable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @ReadOnlyComposable
        @Composable
        get() = MaterialTheme.shapes
}

typealias SvenskaIcons = Icons.Outlined

// TODO: Find nice colors, if no dynamic color scheme is available
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
)

private val LocalSvenskaColors = staticCompositionLocalOf<ColorScheme> {
    error("No SvenskaColors provided")
}

@Composable
fun SvenskaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
    ) {
        CompositionLocalProvider(
            value = LocalSvenskaColors provides colorScheme,
            content = content,
        )
    }
}
