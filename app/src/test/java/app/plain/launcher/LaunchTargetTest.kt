package app.plain.launcher

import app.plain.launcher.apps.LaunchTarget
import app.plain.launcher.apps.LauncherActivity
import org.junit.Assert.assertEquals
import org.junit.Test

class LaunchTargetTest {
    @Test
    fun prefersOverrideThenSlotThenStableSort() {
        val activities = listOf(
            LauncherActivity("override"),
            LauncherActivity("z.Activity"),
            LauncherActivity("a.Activity"),
        )
        assertEquals("override", LaunchTarget.choose(activities, "a.Activity", "override"))
        assertEquals("a.Activity", LaunchTarget.choose(activities, "a.Activity", null))
        assertEquals("a.Activity", LaunchTarget.choose(activities, null, null))
    }
}
