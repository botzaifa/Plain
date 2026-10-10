package app.plain.launcher.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Telephony
import android.telecom.TelecomManager

class DefaultsResolver(private val context: Context) {
    private val pm: PackageManager = context.packageManager

    /** Default Basic order: Phone, Camera, Gallery, Messages. */
    fun resolveAll(launchable: Set<String>): List<String> {
        val ordered =
            listOfNotNull(
                dialer(),
                camera(),
                gallery(),
                sms(),
            )
        return ordered.filter { launchable.contains(it) }.distinct()
    }

    fun dialer(): String? {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val tm = context.getSystemService(TelecomManager::class.java)
            return tm?.defaultDialerPackage
        }
        return resolvePackage(Intent(Intent.ACTION_DIAL))
    }

    fun clock(): String? {
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        return resolvePackage(intent)
    }

    fun sms(): String? = Telephony.Sms.getDefaultSmsPackage(context)

    fun contacts(): String? {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "vnd.android.cursor.dir/contact" }
        return resolvePackage(intent)
    }

    fun camera(): String? {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        return resolvePackage(intent)
    }

    fun gallery(): String? {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        return resolvePackage(intent)
    }

    private fun resolvePackage(intent: Intent): String? {
        val resolved = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val pkg = resolved?.activityInfo?.packageName
        if (pkg != null && pkg != "android") return pkg
        val matches = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (ri in matches) {
            val p = ri.activityInfo?.packageName ?: continue
            if (p != "android") return p
        }
        return null
    }
}
