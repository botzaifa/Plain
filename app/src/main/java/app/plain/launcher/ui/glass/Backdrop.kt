package app.plain.launcher.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import app.plain.launcher.ui.theme.PlainBlack

@Composable
fun Backdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PlainBlack)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        PlainBlack.copy(alpha = 0.08f),
                        PlainBlack,
                    ),
                    center = Offset(0.3f, 0.15f),
                    radius = 900f,
                ),
            ),
    )
}
