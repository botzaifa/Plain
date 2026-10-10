package app.plain.launcher.apps

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import app.plain.launcher.R

object Launcher {
    fun launch(context: Context, entry: AppEntry) {
        try {
            val intent =
                Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    component = ComponentName(entry.packageName, entry.activityClassName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            toastFail(context)
        } catch (_: SecurityException) {
            toastFail(context)
        }
    }

    private fun toastFail(context: Context) {
        Toast.makeText(context, R.string.couldnt_open, Toast.LENGTH_SHORT).show()
    }
}
