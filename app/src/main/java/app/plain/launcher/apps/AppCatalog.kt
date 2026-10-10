package app.plain.launcher.apps

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.os.UserHandle
import app.plain.launcher.BuildConfig
import app.plain.launcher.core.Config

class AppCatalog(context: Context) {
    interface Listener {
        fun onCatalogChanged()
    }

    private val appContext = context.applicationContext
    private val launcherApps = appContext.getSystemService(LauncherApps::class.java)
    private val packageManager = appContext.packageManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val listeners = mutableSetOf<Listener>()
    private var entries: List<AppEntry> = emptyList()

    private val callback =
        object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                mainHandler.postDelayed({
                    if (launcherApps?.getActivityList(packageName, user).isNullOrEmpty()) {
                        refresh()
                    }
                }, Config.PACKAGE_ABSENCE_DELAY_MS)
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                refresh()
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                refresh()
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) {
                refresh()
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) {
                if (replacing) return
                for (pkg in packageNames) {
                    mainHandler.postDelayed({
                        if (launcherApps?.getActivityList(pkg, user).isNullOrEmpty()) {
                            refresh()
                        }
                    }, Config.PACKAGE_ABSENCE_DELAY_MS)
                }
            }
        }

    init {
        refresh()
        launcherApps?.registerCallback(callback, mainHandler)
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    fun getLaunchable(): List<AppEntry> = entries

    fun launchablePackages(): Set<String> = entries.map { it.packageName }.toSet()

    fun findEntry(packageName: String): AppEntry? = entries.firstOrNull { it.packageName == packageName }

    fun refresh() {
        entries = loadEntries()
        listeners.forEach { it.onCatalogChanged() }
    }

    private fun loadEntries(): List<AppEntry> {
        val user = Process.myUserHandle()
        val fromLauncher = launcherApps?.getActivityList(null, user).orEmpty()
        val mapped =
            if (fromLauncher.isNotEmpty()) {
                fromLauncher.map { info ->
                    AppEntry(
                        packageName = info.applicationInfo.packageName,
                        label = info.label.toString(),
                        activityClassName = info.name,
                        activityInfo = info,
                    )
                }
            } else {
                fallbackFromPackageManager()
            }
        return mapped
            .filter { it.packageName != BuildConfig.APPLICATION_ID }
            .distinctBy { it.packageName }
            .sortedBy { it.sortKey }
    }

    /**
     * On newer devices [LauncherApps.getActivityList] can return empty until Plain holds the HOME
     * role. PackageManager still exposes launcher activities via manifest queries.
     */
    private fun fallbackFromPackageManager(): List<AppEntry> {
        val intent =
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()),
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            }
        return resolved.mapNotNull { ri ->
            val ai = ri.activityInfo ?: return@mapNotNull null
            val pkg = ai.packageName ?: return@mapNotNull null
            val label = ri.loadLabel(packageManager)?.toString() ?: pkg
            AppEntry(
                packageName = pkg,
                label = label,
                activityClassName = ai.name,
                activityInfo = null,
            )
        }
    }
}
