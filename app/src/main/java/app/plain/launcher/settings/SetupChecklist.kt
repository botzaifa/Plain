package app.plain.launcher.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import app.plain.launcher.ui.components.TextRow
import app.plain.launcher.ui.theme.LocalPlainTypography

@Composable
fun SetupChecklist(
    packageNames: List<String>,
    keepOn: Set<String>,
    modifier: Modifier = Modifier,
) {
    val type = LocalPlainTypography.current
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        packageNames.forEach { pkg ->
            val note = if (pkg in keepOn) " (keep on)" else ""
            TextRow(
                text = "$pkg$note",
                style = type.body,
                onClick = {
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, pkg)
                    }
                    context.startActivity(intent)
                },
            )
        }
    }
}
