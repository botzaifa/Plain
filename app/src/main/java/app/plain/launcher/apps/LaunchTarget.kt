package app.plain.launcher.apps

data class LauncherActivity(
    val activityClassName: String,
)

object LaunchTarget {
    fun choose(
        activities: List<LauncherActivity>,
        slotPickedActivityClass: String?,
        userOverrideActivityClass: String?,
    ): String? {
        if (activities.isEmpty()) return null
        userOverrideActivityClass?.let { override ->
            if (activities.any { it.activityClassName == override }) return override
        }
        slotPickedActivityClass?.let { slot ->
            if (activities.any { it.activityClassName == slot }) return slot
        }
        return activities
            .sortedBy { it.activityClassName }
            .first()
            .activityClassName
    }
}
