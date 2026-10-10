package app.plain.launcher.system

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build

object KeyguardHelper {
    /**
     * Whether the system keyguard still blocks the user. Plain uses [KeyguardManager.isKeyguardLocked]
     * only — on some OEMs (e.g. Vivo Android 10) [KeyguardManager.isDeviceLocked] stays true after the
     * PIN was entered, which would leave Home invisible forever if OR'd with keyguard state.
     */
    fun isLocked(context: Context): Boolean {
        val km = context.getSystemService(KeyguardManager::class.java) ?: return false
        @Suppress("DEPRECATION")
        return km.isKeyguardLocked
    }

    fun whenUnlocked(
        activity: Activity,
        onUnlocked: () -> Unit,
        onStillLocked: () -> Unit = {},
    ) {
        if (!isLocked(activity)) {
            onUnlocked()
            return
        }
        onStillLocked()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val km = activity.getSystemService(KeyguardManager::class.java) ?: return
            km.requestDismissKeyguard(
                activity,
                object : KeyguardManager.KeyguardDismissCallback() {
                    override fun onDismissSucceeded() {
                        onUnlocked()
                    }

                    override fun onDismissError() {
                        if (!isLocked(activity)) {
                            onUnlocked()
                        }
                    }

                    override fun onDismissCancelled() {
                        if (!isLocked(activity)) {
                            onUnlocked()
                        }
                    }
                },
            )
        }
    }
}
