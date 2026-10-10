package app.plain.launcher.gate

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.plain.launcher.data.AppRepository
import app.plain.launcher.ui.components.TextRow
import app.plain.launcher.ui.theme.LocalPlainTypography

@Composable
fun GatedListScreen(
    repository: AppRepository,
    onLaunch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val type = LocalPlainTypography.current
    val apps by repository.gatedApps().collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        apps.forEach { app ->
            TextRow(text = app.label, style = type.body, onClick = { onLaunch(app.packageName) })
        }
    }
}
