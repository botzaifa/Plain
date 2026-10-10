package app.plain.launcher.system

import android.app.Activity
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import app.plain.launcher.BuildConfig

object HomeRole {
    fun isHomeHeld(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java)
            return rm?.isRoleHeld(RoleManager.ROLE_HOME) == true
        }
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val resolved = context.packageManager.resolveActivity(home, PackageManager.MATCH_DEFAULT_ONLY)
        return resolved?.activityInfo?.packageName == BuildConfig.APPLICATION_ID
    }

    fun createRequestIntent(context: Context): Intent? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val rm = context.getSystemService(RoleManager::class.java) ?: return null
            if (rm.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return rm.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        return fallbackIntent(context)
    }

    fun fallbackIntent(context: Context): Intent? {
        val homeSettings = Intent(Settings.ACTION_HOME_SETTINGS)
        if (homeSettings.resolveActivity(context.packageManager) != null) {
            return homeSettings
        }
        val manage = Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS)
        if (manage.resolveActivity(context.packageManager) != null) {
            return manage
        }
        return Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    }

    fun launchRequest(activity: Activity, requestCode: Int) {
        val intent = createRequestIntent(activity) ?: fallbackIntent(activity)
        if (intent != null) {
            activity.startActivityForResult(intent, requestCode)
        }
    }
}
