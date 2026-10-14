package app.plain.launcher.enforce

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings

/** Android 13+ blocks sideloaded apps from enabling accessibility until the user allows restricted settings in App info. */
object RestrictedSettingsHelper {
    private const val OP_ACCESS_RESTRICTED_SETTINGS = "android:access_restricted_settings"

    fun needsAllowRestrictedSettings(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        return !hasRestrictedSettingsAccess(context)
    }

    fun hasRestrictedSettingsAccess(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return true
        val mode =
            try {
                appOps.checkOpNoThrow(
                    OP_ACCESS_RESTRICTED_SETTINGS,
                    Process.myUid(),
                    context.packageName,
                )
            } catch (_: SecurityException) {
                // API 34+: querying this op is not permitted for normal apps; treat as allowed so we do not crash.
                return true
            }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openAppInfo(context: Context) {
        val intent =
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                if (context !is android.app.Activity) {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
        context.startActivity(intent)
    }
}
