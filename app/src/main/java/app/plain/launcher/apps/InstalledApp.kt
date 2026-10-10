package app.plain.launcher.apps

data class InstalledApp(
    val packageName: String,
    val label: String,
    val activities: List<LauncherActivity>,
    val available: Boolean = true,
)
