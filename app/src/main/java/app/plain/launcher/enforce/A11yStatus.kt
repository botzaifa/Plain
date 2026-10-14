package app.plain.launcher.enforce

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import android.text.TextUtils
import app.plain.launcher.BuildConfig
enum class A11yHealth {
    OFF,
    ENABLED_BUT_DEAD,
    RUNNING,
}

object A11yStatus {
    fun health(context: Context): A11yHealth {
        val cn = ComponentName(BuildConfig.APPLICATION_ID, GateAccessibilityService::class.java.name)
        val enabled = isServiceEnabled(context, cn)
        if (!enabled) return A11yHealth.OFF
        if (GateAccessibilityService.instance != null) return A11yHealth.RUNNING
        val sinceConnect = SystemClock.elapsedRealtime() - GateAccessibilityService.lastConnectedElapsed
        if (sinceConnect in 0..3000L) return A11yHealth.RUNNING
        return A11yHealth.ENABLED_BUT_DEAD
    }

    fun openSettings(context: Context) {
        val intent = android.content.Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        if (context !is Activity) {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun isServiceEnabled(context: Context, cn: ComponentName): Boolean {
        val enabled =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
                ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabled)
        while (splitter.hasNext()) {
            val component = ComponentName.unflattenFromString(splitter.next()) ?: continue
            if (component == cn) return true
        }
        return false
    }
}
