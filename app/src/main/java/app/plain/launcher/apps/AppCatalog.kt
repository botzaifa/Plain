package app.plain.launcher.apps

import android.content.pm.LauncherApps
import android.os.Process
import android.os.UserHandle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppCatalog(
    private val scope: CoroutineScope,
    private val launcherApps: LauncherApps,
    private val ownPackageName: String,
    private val packagePresence: PackagePresence,
    private val onPackageAdded: suspend (String) -> Unit,
    private val onPackageRemoved: suspend (String) -> Unit,
    private val onPackageChanged: suspend (String) -> Unit,
    private val reconcileStoredPackages: suspend (Set<String>) -> Unit,
) : AppCatalogReader {
    private val user: UserHandle = Process.myUserHandle()

    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    override val apps: StateFlow<List<InstalledApp>> = _apps.asStateFlow()

    private var callbackRegistered = false

    private val callback =
        object : LauncherApps.Callback() {
            override fun onPackageAdded(packageName: String, user: UserHandle) {
                if (user != Process.myUserHandle()) return
                if (packageName == ownPackageName) return
                refreshPackage(packageName)
                scope.launch { onPackageAdded(packageName) }
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                if (user != Process.myUserHandle()) return
                if (packageName == ownPackageName) return
                refreshPackage(packageName)
                scope.launch { onPackageChanged(packageName) }
            }

            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                if (user != Process.myUserHandle()) return
                if (packageName == ownPackageName) return
                removePackage(packageName)
                scope.launch { onPackageRemoved(packageName) }
            }

            override fun onPackagesUnavailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) {
                if (user != Process.myUserHandle()) return
                markUnavailable(packageNames.toSet(), unavailable = true)
            }

            override fun onPackagesAvailable(
                packageNames: Array<out String>,
                user: UserHandle,
                replacing: Boolean,
            ) {
                if (user != Process.myUserHandle()) return
                markUnavailable(packageNames.toSet(), unavailable = false)
            }
        }

    fun start() {
        if (!callbackRegistered) {
            launcherApps.registerCallback(callback)
            callbackRegistered = true
        }
        refreshAll()
    }

    fun stop() {
        if (callbackRegistered) {
            launcherApps.unregisterCallback(callback)
            callbackRegistered = false
        }
    }

    fun refreshAll() {
        val list = launcherApps.getActivityList(null, user)
        if (list.isEmpty()) {
            return
        }
        val grouped = list
            .filter { it.applicationInfo.packageName != ownPackageName }
            .groupBy { it.applicationInfo.packageName }
            .map { (pkg, activities) ->
                val label = activities.first().label?.toString() ?: pkg
                InstalledApp(
                    packageName = pkg,
                    label = label,
                    activities = activities.map { LauncherActivity(it.componentName.className) },
                    available = true,
                )
            }
            .sortedBy { it.label.lowercase() }
        _apps.value = grouped

        if (packagePresence.reconciliationEnabled) {
            scope.launch {
                reconcileStoredPackages(grouped.map { it.packageName }.toSet())
            }
        }
    }

    override fun activitiesFor(packageName: String): List<LauncherActivity> {
        return launcherApps.getActivityList(packageName, user).map {
            LauncherActivity(it.componentName.className)
        }
    }

    private fun refreshPackage(packageName: String) {
        val activities = launcherApps.getActivityList(packageName, user)
        if (activities.isEmpty()) {
            removePackage(packageName)
            return
        }
        val label = activities.first().label?.toString() ?: packageName
        val app = InstalledApp(
            packageName = packageName,
            label = label,
            activities = activities.map { LauncherActivity(it.componentName.className) },
            available = true,
        )
        _apps.value = (_apps.value.filter { it.packageName != packageName } + app)
            .sortedBy { it.label.lowercase() }
    }

    private fun removePackage(packageName: String) {
        _apps.value = _apps.value.filter { it.packageName != packageName }
    }

    private fun markUnavailable(packages: Set<String>, unavailable: Boolean) {
        _apps.value = _apps.value.map { app ->
            if (app.packageName in packages) app.copy(available = !unavailable) else app
        }
    }
}
