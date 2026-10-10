package app.plain.launcher

import android.app.Application
import app.plain.launcher.apps.AlwaysAllowedResolver
import app.plain.launcher.apps.AppCatalog
import app.plain.launcher.apps.DefaultsResolver
import app.plain.launcher.apps.IconLoader
import app.plain.launcher.auth.AuthRepository
import app.plain.launcher.core.AndroidClock
import app.plain.launcher.core.KeyValueStore
import app.plain.launcher.core.SharedPreferencesStore
import app.plain.launcher.core.TempDuration
import app.plain.launcher.data.BasicStore
import app.plain.launcher.enforce.GateAccessibilityService
import app.plain.launcher.policy.LoopGuard
import app.plain.launcher.settings.AdminSession
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class PlainApp : Application() {
    lateinit var store: KeyValueStore
        private set
    lateinit var clock: AndroidClock
        private set
    lateinit var auth: AuthRepository
        private set
    lateinit var catalog: AppCatalog
        private set
    lateinit var defaults: DefaultsResolver
        private set
    lateinit var basicStore: BasicStore
        private set
    lateinit var iconLoader: IconLoader
        private set
    lateinit var alwaysAllowed: AlwaysAllowedResolver
        private set
    lateinit var adminSession: AdminSession
        private set
    lateinit var backgroundExecutor: Executor
        private set

    val loopGuard = LoopGuard()

    @Volatile
    var alwaysAllowedCache: Set<String> = emptySet()
        private set

    @Volatile
    var enforcementPausedUntil: Long = 0L

    @Volatile
    var lastLoopGuardTrip: LoopGuardTrip? = null

    data class LoopGuardTrip(val timeMs: Long, val packageName: String?)

    override fun onCreate() {
        super.onCreate()
        instance = this
        store = SharedPreferencesStore(this, "plain_v2")
        clock = AndroidClock(this)
        backgroundExecutor = Executors.newSingleThreadExecutor()
        auth =
            AuthRepository(store, clock) {
                TempDuration.currentMs(store)
            }
        catalog = AppCatalog(this)
        defaults = DefaultsResolver(this)
        basicStore = BasicStore(store)
        iconLoader = IconLoader(this, catalog, backgroundExecutor)
        alwaysAllowed = AlwaysAllowedResolver(this, defaults)
        adminSession = AdminSession(store, clock)
        refreshAlwaysAllowed()
        catalog.addListener(object : AppCatalog.Listener {
            override fun onCatalogChanged() {
                refreshAlwaysAllowed()
                seedBasicIfEmpty()
            }
        })
        seedBasicIfEmpty()
        val tripTime = store.getLong(GateAccessibilityService.KEY_LOOP_TRIP_TIME, 0L)
        if (tripTime > 0L) {
            lastLoopGuardTrip =
                LoopGuardTrip(tripTime, store.getString(GateAccessibilityService.KEY_LOOP_TRIP_PKG, null))
        }
    }

    fun refreshAlwaysAllowed() {
        alwaysAllowedCache = alwaysAllowed.resolve()
    }

    private fun seedBasicIfEmpty() {
        if (basicStore.getOrdered().isNotEmpty()) return
        if (!auth.isSetupComplete()) return
        val launchable = catalog.launchablePackages()
        val defs = defaults.resolveAll(launchable)
        basicStore.setOrdered(defs)
    }

    fun onTrimMemorySafe() {
        iconLoader.trimMemory()
    }

    companion object {
        @Volatile
        lateinit var instance: PlainApp
            private set
    }
}
