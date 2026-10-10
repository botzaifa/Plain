package app.plain.launcher.system

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.os.Build

object KeyguardHelper {
    fun isLocked(context: Context): Boolean {
        val km = context.getSystemService(KeyguardManager::class.java) ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            km.isDeviceLocked || km.isKeyguardLocked
        } else {
            @Suppress("DEPRECATION")
            km.isKeyguardLocked
        }
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
                },
            )
        }
    }
}
