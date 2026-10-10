package app.plain.launcher.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.plain.launcher.ui.components.TextRow
import app.plain.launcher.ui.glass.GlassSurface
import app.plain.launcher.ui.theme.LocalPlainTypography
import app.plain.launcher.ui.theme.PlainSecondary
import app.plain.launcher.ui.theme.PlainWhite

@Composable
fun SearchSheet(
    viewModel: SearchViewModel,
    hazeState: Any?,
    onLaunchApp: (String) -> Unit,
    onAllApps: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val type = LocalPlainTypography.current
    val results by viewModel.results.collectAsStateWithLifecycle()
    val query by viewModel.queryText.collectAsStateWithLifecycle()

    GlassSurface(hazeState = hazeState, modifier = modifier.fillMaxWidth().padding(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            BasicTextField(
                value = query,
                onValueChange = viewModel::setQuery,
                textStyle = type.body.copy(color = PlainWhite),
                cursorBrush = SolidColor(PlainWhite),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        BasicText("search apps", style = type.hint)
                    }
                    inner()
                },
            )
            results.forEach { app ->
                TextRow(text = app.label, style = type.body, onClick = { onLaunchApp(app.packageName) })
            }
            TextRow(
                text = "all apps",
                style = type.hint.copy(color = PlainSecondary),
                onClick = onAllApps,
            )
        }
    }
}
