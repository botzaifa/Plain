package app.plain.launcher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import app.plain.launcher.ui.theme.LocalPlainTypography
import app.plain.launcher.ui.theme.PlainWhite

@Composable
fun Wordmark(modifier: Modifier = Modifier) {
    val type = LocalPlainTypography.current
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        BasicText(text = "plain", style = type.title)
        Spacer(modifier = Modifier.size(6.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(PlainWhite.copy(alpha = 0.35f)),
        )
    }
}
