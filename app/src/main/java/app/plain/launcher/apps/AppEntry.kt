package app.plain.launcher.apps

import android.content.pm.LauncherActivityInfo

data class AppEntry(
    val packageName: String,
    val label: String,
    val activityClassName: String,
    /** Null when loaded via PackageManager fallback (no HOME role yet). */
    val activityInfo: LauncherActivityInfo?,
) {
    val sortKey: String get() = label.lowercase()
}
