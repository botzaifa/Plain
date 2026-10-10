package app.plain.launcher.apps

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle

sealed interface LaunchResult {
    data object Accepted : LaunchResult
    data class Failed(val reason: String) : LaunchResult
}

class AppLauncher(
    private val context: Context,
    private val launcherApps: LauncherApps,
) {
    fun launch(packageName: String, activityClassName: String): LaunchResult {
        return try {
            val component = ComponentName(packageName, activityClassName)
            val user: UserHandle = Process.myUserHandle()
            launcherApps.startMainActivity(component, user, null, null)
            LaunchResult.Accepted
        } catch (e: Exception) {
            when (e) {
                is android.content.ActivityNotFoundException,
                is SecurityException,
                -> LaunchResult.Failed(e.message ?: e.javaClass.simpleName)
                else -> LaunchResult.Failed(e.message ?: e.javaClass.simpleName)
            }
        }
    }

    fun launchIntent(intent: android.content.Intent): LaunchResult {
        return try {
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            LaunchResult.Accepted
        } catch (e: Exception) {
            LaunchResult.Failed(e.message ?: e.javaClass.simpleName)
        }
    }
}
