package app.plain.launcher.core

import android.content.Context
import android.os.SystemClock
import android.provider.Settings

interface Clock {
    fun elapsedRealtime(): Long
    fun bootCount(): Int?
}

class AndroidClock(context: Context) : Clock {
    private val appContext = context.applicationContext

    override fun elapsedRealtime(): Long = SystemClock.elapsedRealtime()

    override fun bootCount(): Int? {
        return try {
            Settings.Global.getInt(appContext.contentResolver, Settings.Global.BOOT_COUNT)
        } catch (_: Settings.SettingNotFoundException) {
            null
        }
    }
}
