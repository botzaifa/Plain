package app.plain.launcher.apps

import android.content.pm.PackageManager
import android.os.UserHandle
import android.content.pm.LauncherApps

/**
 * Per-package install check for startup reconciliation.
 *
 * M2 device validation pending: startup reconciliation stays disabled until
 * [reconciliationEnabled] is turned on after verifying which API wins on device.
 * Tries getPackageInfo, getApplicationInfo, and isPackageEnabled in order.
 */
class PackagePresence(
    private val packageManager: PackageManager,
    private val launcherApps: LauncherApps,
    private val user: UserHandle,
) : PackagePresenceChecker {
    override var reconciliationEnabled: Boolean = false

    override fun isAbsent(packageName: String): Boolean = !isPresent(packageName)

    fun isPresent(packageName: String): Boolean {
        if (tryGetPackageInfo(packageName)) return true
        if (tryGetApplicationInfo(packageName)) return true
        if (tryIsPackageEnabled(packageName)) return true
        return false
    }

    private fun tryGetPackageInfo(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun tryGetApplicationInfo(packageName: String): Boolean {
        return try {
            launcherApps.getApplicationInfo(packageName, 0, user)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun tryIsPackageEnabled(packageName: String): Boolean {
        return try {
            launcherApps.isPackageEnabled(packageName, user)
        } catch (_: Exception) {
            false
        }
    }
}
