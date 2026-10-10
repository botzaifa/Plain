package app.plain.launcher

import android.content.Context
import androidx.room.Room
import app.plain.launcher.apps.AppCatalog
import app.plain.launcher.apps.AppLauncher
import app.plain.launcher.apps.PackagePresence
import app.plain.launcher.apps.SlotResolver
import app.plain.launcher.data.AppRepository
import app.plain.launcher.data.FirstRunStore
import app.plain.launcher.data.GateRepository
import app.plain.launcher.data.PlainDatabase
import app.plain.launcher.gate.GateSession
import app.plain.launcher.home.NextAlarmSource
import app.plain.launcher.home.TimeSource
import java.time.Clock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    val applicationScope = CoroutineScope(SupervisorJob())

    val clock: Clock = Clock.systemDefaultZone()

    private val launcherApps =
        appContext.getSystemService(android.content.pm.LauncherApps::class.java)!!

    val packagePresence = PackagePresence(
        appContext.packageManager,
        launcherApps,
        android.os.Process.myUserHandle(),
    )

    val database: PlainDatabase =
        Room.databaseBuilder(appContext, PlainDatabase::class.java, "plain.db")
            .fallbackToDestructiveMigration()
            .build()

    val firstRunStore = FirstRunStore(appContext)

    val gateSession = GateSession()

    val slotResolver = SlotResolver(appContext)

    val appLauncher = AppLauncher(appContext, launcherApps)

    val gateRepository = GateRepository(database.gatePassDao(), clock)

    lateinit var appRepository: AppRepository
        private set

    lateinit var appCatalog: AppCatalog
        private set

    val timeSource = TimeSource(appContext)
    val nextAlarmSource = NextAlarmSource(appContext)

    init {
        appCatalog = AppCatalog(
            scope = applicationScope,
            launcherApps = launcherApps,
            ownPackageName = appContext.packageName,
            packagePresence = packagePresence,
            onPackageAdded = { pkg -> appRepository.onPackageAdded(pkg) },
            onPackageRemoved = { pkg -> appRepository.onPackageRemoved(pkg) },
            onPackageChanged = { pkg -> appRepository.onPackageChanged(pkg) },
            reconcileStoredPackages = { present -> appRepository.reconcileStored(present) },
        )
        appRepository = AppRepository(
            restoreDefaultsGateway = database,
            appEntryDao = database.appEntryDao(),
            slotOverrideDao = database.slotOverrideDao(),
            launchOverrideDao = database.launchOverrideDao(),
            firstRunStore = firstRunStore,
            slotResolver = slotResolver,
            appCatalog = appCatalog,
            packagePresence = packagePresence,
            gateSession = gateSession,
            ownPackageName = appContext.packageName,
        )
    }

    fun startCatalog() {
        appCatalog.start()
    }

    fun stopCatalog() {
        appCatalog.stop()
    }
}
