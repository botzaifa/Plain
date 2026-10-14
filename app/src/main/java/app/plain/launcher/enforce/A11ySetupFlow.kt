package app.plain.launcher.enforce

import android.app.Activity
import android.app.AlertDialog
import app.plain.launcher.R

object A11ySetupFlow {
    /** Opens App info first when Android blocked the service (restricted settings), else accessibility settings. */
    fun start(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed) return
        if (RestrictedSettingsHelper.needsAllowRestrictedSettings(activity)) {
            AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                .setTitle(R.string.restricted_settings_title)
                .setMessage(R.string.restricted_settings_message)
                .setPositiveButton(R.string.open_app_info) { _, _ ->
                    RestrictedSettingsHelper.openAppInfo(activity)
                }
                .setNeutralButton(R.string.open_accessibility) { _, _ ->
                    A11yStatus.openSettings(activity)
                }
                .setNegativeButton(android.R.string.cancel, null)
                .show()
            return
        }
        A11yStatus.openSettings(activity)
    }
}
