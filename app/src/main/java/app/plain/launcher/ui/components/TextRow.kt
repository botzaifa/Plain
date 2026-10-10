package app.plain.launcher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun TextRow(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val base = modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp, horizontal = 4.dp)
    if (onClick != null) {
        BasicText(
            text = text,
            style = style,
            modifier = base.clickable(onClick = onClick),
        )
    } else {
        BasicText(text = text, style = style, modifier = base)
    }
}
