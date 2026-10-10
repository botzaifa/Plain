package app.plain.launcher.apps

import kotlinx.coroutines.flow.StateFlow

interface AppCatalogReader {
    val apps: StateFlow<List<InstalledApp>>
    fun activitiesFor(packageName: String): List<LauncherActivity>
}
