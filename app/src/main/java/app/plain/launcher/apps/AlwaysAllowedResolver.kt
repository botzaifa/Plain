package app.plain.launcher.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.inputmethod.InputMethodManager
import app.plain.launcher.BuildConfig
import app.plain.launcher.policy.BuiltInAllowlist

class AlwaysAllowedResolver(
    private val context: Context,
    private val defaults: DefaultsResolver,
) {
    fun resolve(): Set<String> {
        val out = linkedSetOf<String>()
        out.add(BuildConfig.APPLICATION_ID)
        defaults.dialer()?.let { out.add(it) }
        defaults.clock()?.let { out.add(it) }
        out.addAll(homeHandlers())
        out.addAll(enabledImes())
        out.addAll(BuiltInAllowlist.packages)
        return out
    }

    private fun homeHandlers(): Set<String> {
        val intent =
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val pm = context.packageManager
        return pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            .mapNotNull { it.activityInfo?.packageName }
            .toSet()
    }

    /**
     * Apps targeting SDK 34+ cannot read [android.provider.Settings.Secure.ENABLED_INPUT_METHODS].
     * Use [InputMethodManager.getEnabledInputMethodList] on API 34+ only.
     */
    private fun enabledImes(): Set<String> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return emptySet()
        }
        val imm = context.getSystemService(InputMethodManager::class.java) ?: return emptySet()
        return runCatching {
            imm.enabledInputMethodList.map { it.packageName }.toSet()
        }.getOrDefault(emptySet())
    }
}
