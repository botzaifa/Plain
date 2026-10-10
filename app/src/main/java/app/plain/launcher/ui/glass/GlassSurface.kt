package app.plain.launcher.ui.glass

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun GlassSurface(
    hazeState: Any?,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    if (GlassConfig.GLASS_ENABLED) {
        // Haze wiring added when GLASS_ENABLED is turned on after on-device perf check.
        FallbackGlass(modifier, shape, content)
    } else {
        FallbackGlass(modifier, shape, content)
    }
}

@Composable
private fun FallbackGlass(
    modifier: Modifier,
    shape: RoundedCornerShape,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(Color.White.copy(alpha = 0.06f), shape)
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), shape),
    ) {
        content()
    }
}
