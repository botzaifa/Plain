package app.plain.launcher.data

import app.plain.launcher.apps.AppCatalogReader
import app.plain.launcher.apps.LaunchTarget
import app.plain.launcher.apps.PackagePresenceChecker
import app.plain.launcher.apps.ResolvedSlot
import app.plain.launcher.apps.Slot
import app.plain.launcher.apps.SlotDecision
import app.plain.launcher.apps.SlotResolverReader
import app.plain.launcher.apps.Tier
import app.plain.launcher.gate.GateSessionController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

data class DisplayApp(
    val packageName: String,
    val label: String,
    val tier: Tier,
    val position: Int = 0,
)

data class HomeSlotDisplay(
    val slot: Slot,
    val label: String,
    val packageName: String?,
    val resolved: Boolean,
)

class RepositoryException(message: String) : Exception(message)

class AppRepository(
    private val restoreDefaultsGateway: RestoreDefaultsGateway,
    private val appEntryDao: AppEntryDao,
    private val slotOverrideDao: SlotOverrideDao,
    private val launchOverrideDao: LaunchOverrideDao,
    private val firstRunStore: FirstRunPreferences,
    private val slotResolver: SlotResolverReader,
    private val appCatalog: AppCatalogReader,
    private val packagePresence: PackagePresenceChecker,
    private val gateSession: GateSessionController,
    private val ownPackageName: String,
) {
    val allEntries: Flow<List<AppEntryEntity>> = appEntryDao.observeAll()

    fun homeSlots(): Flow<List<HomeSlotDisplay>> {
        return combine(appEntryDao.observeAll(), slotOverrideDao.observeAll()) { entries, overrides ->
            val overrideMap = overrides.associate { it.slot to it.packageName }
            Slot.homeOrder.map { slot ->
                val resolved = slotResolver.resolve(slot, overrideMap[slot.storageKey])
                val pkg = (resolved.decision as? SlotDecision.Resolved)?.packageName
                val label = pkg?.let { labelFor(it) } ?: slot.name
                HomeSlotDisplay(slot, label, pkg, pkg != null)
            }.filter { it.resolved }
        }
    }

    fun toolsApps(): Flow<List<DisplayApp>> {
        return appEntryDao.observeAll().map { entries ->
            entries.filter { it.tier == Tier.TOOLS.name }
                .map { DisplayApp(it.packageName, labelFor(it.packageName), Tier.TOOLS) }
                .sortedBy { it.label.lowercase() }
        }
    }

    fun gatedApps(): Flow<List<DisplayApp>> {
        return combine(appEntryDao.observeAll(), slotOverrideDao.observeAll()) { entries, overrides ->
            val overrideMap = overrides.associate { it.slot to it.packageName }
            val internet = resolvedPackage(Slot.Internet, overrideMap)
            val duolingo = resolvedPackage(Slot.Duolingo, overrideMap)
            val gated = entries.filter { it.tier == Tier.GATED.name }
                .map { DisplayApp(it.packageName, labelFor(it.packageName), Tier.GATED) }
            TierRules.sortGatedLabels(
                gated.map { it.packageName to it.label },
                internet,
                duolingo,
            ).map { (pkg, label) -> DisplayApp(pkg, label, Tier.GATED) }
        }
    }

    fun searchCandidates(): Flow<List<DisplayApp>> {
        return combine(homeSlots(), toolsApps()) { home, tools ->
            val homeApps = home.mapNotNull { slot ->
                slot.packageName?.let {
                    DisplayApp(it, slot.label, Tier.HOME, Slot.homeOrder.indexOf(slot.slot))
                }
            }
            homeApps + tools
        }
    }

    fun unresolvedSlots(): Flow<List<ResolvedSlot>> {
        return slotOverrideDao.observeAll().map { overrides ->
            val overrideMap = overrides.associate { it.slot to it.packageName }
            enumValues<Slot>().map { slotResolver.resolve(it, overrideMap[it.storageKey]) }
                .filter {
                    it.decision is SlotDecision.Unresolved || it.decision is SlotDecision.Ambiguous
                }
        }
    }

    suspend fun ensureFirstRun() {
        val done = firstRunStore.firstRunDone.first()
        if (done) return
        seedDefaults()
        firstRunStore.setFirstRunDone()
    }

    suspend fun onPackageAdded(packageName: String) {
        if (packageName == ownPackageName) return
        if (appEntryDao.get(packageName) != null) return
        val tier = tierForNewPackage(packageName)
        val position = if (tier == Tier.HOME) nextHomePosition() else 0
        appEntryDao.upsert(
            AppEntryEntity(packageName, tier.name, position, null),
        )
    }

    suspend fun onPackageRemoved(packageName: String) {
        appEntryDao.delete(packageName)
        launchOverrideDao.delete(packageName)
    }

    suspend fun onPackageChanged(packageName: String) {
        if (appEntryDao.get(packageName) == null) {
            onPackageAdded(packageName)
        }
    }

    suspend fun reconcileStored(stillPresent: Set<String>) {
        if (!packagePresence.reconciliationEnabled) return
        val stored = appEntryDao.getAll().map { it.packageName }
        for (pkg in stored) {
            if (pkg in stillPresent) continue
            if (packagePresence.isAbsent(pkg)) {
                onPackageRemoved(pkg)
            }
        }
    }

    suspend fun setTier(packageName: String, newTier: Tier) {
        val entry = appEntryDao.get(packageName) ?: return
        val oldTier = Tier.valueOf(entry.tier)
        val slotMap = slotPackageMap()
        if (!TierRules.canAssignTier(packageName, newTier, slotMap)) {
            throw RepositoryException("Protected app cannot be gated")
        }
        if (TierRules.requiresApplyAuthorization(oldTier, newTier)) {
            if (!gateSession.consumeApplyAuthorization()) {
                throw RepositoryException("Apply authorization required")
            }
        }
        val position = if (newTier == Tier.HOME) entry.position else 0
        appEntryDao.upsert(entry.copy(tier = newTier.name, position = position))
    }

    suspend fun moveHome(packageName: String, direction: Int) {
        val entries = appEntryDao.getAll().filter { it.tier == Tier.HOME.name }.sortedBy { it.position }
        val index = entries.indexOfFirst { it.packageName == packageName }
        if (index < 0) return
        val swapIndex = index + direction
        if (swapIndex !in entries.indices) return
        val a = entries[index]
        val b = entries[swapIndex]
        appEntryDao.upsert(a.copy(position = b.position))
        appEntryDao.upsert(b.copy(position = a.position))
    }

    suspend fun setSlotOverride(slot: Slot, packageName: String) {
        val entry = appEntryDao.get(packageName)
        val oldTier = entry?.let { Tier.valueOf(it.tier) } ?: Tier.GATED
        if (entry != null && TierRules.requiresApplyAuthorization(oldTier, slot.defaultTier)) {
            if (!gateSession.consumeApplyAuthorization()) {
                throw RepositoryException("Apply authorization required")
            }
        }
        slotOverrideDao.upsert(SlotOverrideEntity(slot.storageKey, packageName))
        val tier = slot.defaultTier
        val position = if (tier == Tier.HOME) {
            Slot.homeOrder.indexOf(slot).takeIf { it >= 0 } ?: nextHomePosition()
        } else {
            0
        }
        appEntryDao.upsert(
            AppEntryEntity(packageName, tier.name, position, null),
        )
    }

    suspend fun setLaunchOverride(packageName: String, activityClassName: String) {
        launchOverrideDao.upsert(LaunchOverrideEntity(packageName, activityClassName))
    }

    suspend fun launchActivityClass(packageName: String, slotActivityClass: String?): String? {
        val activities = appCatalog.activitiesFor(packageName)
        val override = launchOverrideDao.getActivityClass(packageName)
        return LaunchTarget.choose(activities, slotActivityClass, override)
    }

    suspend fun restoreDefaults() {
        val slotMap = slotPackageMap()
        val current = appEntryDao.getAll()
        val planned = buildDefaultEntries()
        val needsAuth = current.any { old ->
            val new = planned.find { it.packageName == old.packageName }
            new != null && TierRules.requiresApplyAuthorization(Tier.valueOf(old.tier), Tier.valueOf(new.tier))
        } || current.any { old ->
            old.tier == Tier.GATED.name && planned.none { it.packageName == old.packageName }
        }
        if (needsAuth && !gateSession.consumeApplyAuthorization()) {
            throw RepositoryException("Apply authorization required")
        }
        restoreDefaultsGateway.restoreDefaultsInTransaction(planned)
    }

    private suspend fun seedDefaults() {
        val catalog = appCatalog.apps.value
        val rows = mutableListOf<AppEntryEntity>()
        val overrideMap = emptyMap<String, String>()
        for (slot in enumValues<Slot>()) {
            val resolved = slotResolver.resolve(slot, overrideMap[slot.storageKey])
            val pkg = (resolved.decision as? SlotDecision.Resolved)?.packageName ?: continue
            val position = if (slot.defaultTier == Tier.HOME) Slot.homeOrder.indexOf(slot) else 0
            rows.add(AppEntryEntity(pkg, slot.defaultTier.name, position, null))
        }
        for (app in catalog) {
            if (rows.any { it.packageName == app.packageName }) continue
            rows.add(AppEntryEntity(app.packageName, Tier.GATED.name, 0, null))
        }
        appEntryDao.upsertAll(rows)
    }

    private suspend fun buildDefaultEntries(): List<AppEntryEntity> {
        val catalog = appCatalog.apps.value
        val rows = mutableListOf<AppEntryEntity>()
        for (slot in enumValues<Slot>()) {
            val resolved = slotResolver.resolve(slot, null)
            val pkg = (resolved.decision as? SlotDecision.Resolved)?.packageName ?: continue
            val position = if (slot.defaultTier == Tier.HOME) Slot.homeOrder.indexOf(slot) else 0
            rows.add(AppEntryEntity(pkg, slot.defaultTier.name, position, null))
        }
        for (app in catalog) {
            if (rows.any { it.packageName == app.packageName }) continue
            rows.add(AppEntryEntity(app.packageName, Tier.GATED.name, 0, null))
        }
        return rows
    }

    private suspend fun tierForNewPackage(packageName: String): Tier {
        val overrides = slotOverrideDao.getAll().associate { it.slot to it.packageName }
        for (slot in enumValues<Slot>()) {
            val resolved = slotResolver.resolve(slot, overrides[slot.storageKey])
            if ((resolved.decision as? SlotDecision.Resolved)?.packageName == packageName) {
                return slot.defaultTier
            }
        }
        return Tier.GATED
    }

    private suspend fun nextHomePosition(): Int {
        return (appEntryDao.getAll().filter { it.tier == Tier.HOME.name }.maxOfOrNull { it.position } ?: -1) + 1
    }

    private suspend fun slotPackageMap(): Map<Slot, String?> {
        val overrides = slotOverrideDao.getAll().associate { it.slot to it.packageName }
        return enumValues<Slot>().associateWith { slot ->
            val resolved = slotResolver.resolve(slot, overrides[slot.storageKey])
            (resolved.decision as? SlotDecision.Resolved)?.packageName
        }
    }

    private fun resolvedPackage(slot: Slot, overrideMap: Map<String, String>): String? {
        val resolved = slotResolver.resolve(slot, overrideMap[slot.storageKey])
        return (resolved.decision as? SlotDecision.Resolved)?.packageName
    }

    private fun labelFor(packageName: String): String {
        return appCatalog.apps.value.find { it.packageName == packageName }?.label ?: packageName
    }
}
