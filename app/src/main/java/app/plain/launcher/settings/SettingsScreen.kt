package app.plain.launcher.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.plain.launcher.apps.HomeRole
import app.plain.launcher.apps.Tier
import app.plain.launcher.data.AppRepository
import app.plain.launcher.ui.components.TextRow
import app.plain.launcher.ui.components.Wordmark
import app.plain.launcher.ui.glass.GlassSurface
import app.plain.launcher.ui.theme.LocalPlainTypography

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    repository: AppRepository,
    hazeState: Any?,
    onOpenChecklist: () -> Unit,
    onError: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val type = LocalPlainTypography.current
    val entries by viewModel.entries.collectAsStateWithLifecycle()
    val history by viewModel.gateHistory.collectAsStateWithLifecycle()
    val unresolved by viewModel.unresolved.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val homeLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { }

    GlassSurface(hazeState = hazeState, modifier = modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Wordmark()
            TextRow(
                text = "Set as Home app",
                style = type.body,
                onClick = { HomeRole.launchRequest(homeLauncher, context) },
            )
            TextRow(text = "Switch back via Settings → Apps → Default apps → Home app.", style = type.hint)

            TextRow(
                text = "Restore defaults",
                style = type.body,
                onClick = { viewModel.restoreDefaults(onError) },
            )

            history.take(7).forEach { day ->
                TextRow(text = "${day.date}: ${day.count}", style = type.hint)
            }

            unresolved.forEach { slot ->
                TextRow(text = "${slot.slot.name}: tap to pick", style = type.hint)
            }

            entries.sortedBy { it.packageName }.forEach { entry ->
                val label = entry.packageName
                Column {
                    TextRow(text = label, style = type.body)
                    Tier.entries.forEach { tier ->
                        TextRow(
                            text = "  → ${tier.name}",
                            style = type.hint,
                            onClick = { viewModel.setTier(entry.packageName, tier, onError) },
                        )
                    }
                    if (entry.tier == Tier.HOME.name) {
                        TextRow(
                            text = "  move up",
                            style = type.hint,
                            onClick = { viewModel.moveHome(entry.packageName, -1) },
                        )
                        TextRow(
                            text = "  move down",
                            style = type.hint,
                            onClick = { viewModel.moveHome(entry.packageName, 1) },
                        )
                    }
                }
            }

            TextRow(text = "Notification setup checklist", style = type.body, onClick = onOpenChecklist)
        }
    }
}
