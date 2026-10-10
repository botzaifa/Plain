package app.plain.launcher.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

val LocalPlainTypography = staticCompositionLocalOf { PlainTypeScale() }

@Composable
fun PlainTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalPlainTypography provides PlainTypeScale()) {
        content()
    }
}
