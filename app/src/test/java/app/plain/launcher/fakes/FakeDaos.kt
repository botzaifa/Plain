package app.plain.launcher.fakes

import app.plain.launcher.apps.AppCatalogReader
import app.plain.launcher.apps.InstalledApp
import app.plain.launcher.apps.LauncherActivity
import app.plain.launcher.apps.PackagePresenceChecker
import app.plain.launcher.apps.ResolvedSlot
import app.plain.launcher.apps.Slot
import app.plain.launcher.apps.SlotDecision
import app.plain.launcher.apps.SlotResolverReader
import app.plain.launcher.data.AppEntryDao
import app.plain.launcher.data.AppEntryEntity
import app.plain.launcher.data.FirstRunPreferences
import app.plain.launcher.data.LaunchOverrideDao
import app.plain.launcher.data.LaunchOverrideEntity
import app.plain.launcher.data.SlotOverrideDao
import app.plain.launcher.data.SlotOverrideEntity
import app.plain.launcher.gate.GatePurpose
import app.plain.launcher.gate.GateSessionController
import app.plain.launcher.gate.GateSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeAppEntryDao(initial: List<AppEntryEntity> = emptyList()) : AppEntryDao {
    private val state = MutableStateFlow(initial.toList())
    var failOnUpsertAll: Boolean = false

    override fun observeAll(): Flow<List<AppEntryEntity>> = state
    override suspend fun getAll(): List<AppEntryEntity> = state.value
    override suspend fun get(packageName: String): AppEntryEntity? =
        state.value.find { it.packageName == packageName }

    override suspend fun upsert(entry: AppEntryEntity) {
        state.value = state.value.filter { it.packageName != entry.packageName } + entry
    }

    override suspend fun upsertAll(entries: List<AppEntryEntity>) {
        if (failOnUpsertAll) error("fail")
        entries.forEach { upsert(it) }
    }

    override suspend fun delete(packageName: String) {
        state.value = state.value.filter { it.packageName != packageName }
    }

    override suspend fun deleteAll() {
        state.value = emptyList()
    }

    fun snapshot(): List<AppEntryEntity> = state.value
}

class FakeSlotOverrideDao : SlotOverrideDao {
    private val rows = mutableListOf<SlotOverrideEntity>()
    override fun observeAll(): Flow<List<SlotOverrideEntity>> = flowOf(rows.toList())
    override suspend fun getAll(): List<SlotOverrideEntity> = rows.toList()
    override suspend fun getPackage(slot: String): String? = rows.find { it.slot == slot }?.packageName
    override suspend fun upsert(entity: SlotOverrideEntity) {
        rows.removeAll { it.slot == entity.slot }
        rows.add(entity)
    }
    override suspend fun delete(slot: String) {
        rows.removeAll { it.slot == slot }
    }
    override suspend fun deleteAll() {
        rows.clear()
    }
    fun snapshot(): List<SlotOverrideEntity> = rows.toList()
}

class FakeLaunchOverrideDao : LaunchOverrideDao {
    private val rows = mutableListOf<LaunchOverrideEntity>()
    override suspend fun getAll(): List<LaunchOverrideEntity> = rows.toList()
    override suspend fun getActivityClass(packageName: String): String? =
        rows.find { it.packageName == packageName }?.activityClassName
    override suspend fun upsert(entity: LaunchOverrideEntity) {
        rows.removeAll { it.packageName == entity.packageName }
        rows.add(entity)
    }
    override suspend fun delete(packageName: String) {
        rows.removeAll { it.packageName == packageName }
    }
    override suspend fun deleteAll() {
        rows.clear()
    }
    fun snapshot(): List<LaunchOverrideEntity> = rows.toList()
}

class FakeFirstRunPreferences : FirstRunPreferences {
    override val firstRunDone: Flow<Boolean> = flowOf(true)
    override suspend fun setFirstRunDone() {}
}

class FakeGateSession(
    private var applyAuthorized: Boolean = false,
) : GateSessionController {
    private val _state = MutableStateFlow<GateSessionState>(GateSessionState.Idle)
    override val state: StateFlow<GateSessionState> = _state.asStateFlow()
    override fun beginWait(purpose: GatePurpose, totalSeconds: Int, elapsedRealtimeMs: Long) {}
    override fun cancel() {}
    override fun onCountdownFinished() {}
    override fun onForegroundLost() {}
    override fun consumeLaunchAuthorization(): Boolean = false
    override fun consumeApplyAuthorization(): Boolean {
        if (!applyAuthorized) return false
        applyAuthorized = false
        return true
    }
    override fun hasApplyAuthorization(): Boolean = applyAuthorized
    override fun hasLaunchAuthorization(): Boolean = false
}

class FakePackagePresenceChecker : PackagePresenceChecker {
    override var reconciliationEnabled: Boolean = false
    override fun isAbsent(packageName: String): Boolean = false
}

class FakeAppCatalogReader(
    initial: List<InstalledApp> = listOf(InstalledApp("com.game", "Game", emptyList())),
) : AppCatalogReader {
    override val apps = MutableStateFlow(initial)
    override fun activitiesFor(packageName: String): List<LauncherActivity> = emptyList()
}

class FakeSlotResolverReader : SlotResolverReader {
    override fun resolve(slot: Slot, overridePackage: String?): ResolvedSlot {
        return ResolvedSlot(slot, SlotDecision.Unresolved)
    }
}
