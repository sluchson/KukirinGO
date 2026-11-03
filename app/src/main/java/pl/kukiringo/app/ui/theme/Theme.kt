package pl.kukiringo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KukirinColorScheme = darkColorScheme(
    primary = KukirinOrange,
    onPrimary = KukirinBlack,
    primaryContainer = KukirinOrangeDark,
    onPrimaryContainer = KukirinWhite,
    secondary = KukirinOrangeLight,
    onSecondary = KukirinBlack,
    background = KukirinBlack,
    onBackground = KukirinWhite,
    surface = KukirinBlack,
    onSurface = KukirinWhite,
    surfaceVariant = KukirinSurface,
    onSurfaceVariant = KukirinGrey,
    surfaceContainer = KukirinSurface,
    surfaceContainerHigh = KukirinSurfaceHigh,
    outline = KukirinOutline,
)

/** Motyw aplikacji — zawsze ciemny, czarno-pomarańczowy. */
@Composable
fun KukirinTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = KukirinColorScheme,
        typography = KukirinTypography,
        content = content,
    )
}
