package app.plain.launcher

import app.plain.launcher.apps.Tier
import app.plain.launcher.data.AppEntryEntity
import app.plain.launcher.data.AppRepository
import app.plain.launcher.data.RepositoryException
import app.plain.launcher.fakes.FakeAppCatalogReader
import app.plain.launcher.fakes.FakeAppEntryDao
import app.plain.launcher.fakes.FakeFirstRunPreferences
import app.plain.launcher.fakes.FakeGateSession
import app.plain.launcher.fakes.FakeLaunchOverrideDao
import app.plain.launcher.fakes.FakePackagePresenceChecker
import app.plain.launcher.fakes.FakeRestoreDefaultsGateway
import app.plain.launcher.fakes.FakeSlotOverrideDao
import app.plain.launcher.fakes.FakeSlotResolverReader
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class AppRepositoryTest {
    @Test
    fun movingOutOfGatedWithoutAuthFails() = runBlocking {
        val dao = FakeAppEntryDao(
            listOf(AppEntryEntity("com.game", Tier.GATED.name, 0, null)),
        )
        val repo = buildRepo(dao, FakeGateSession())
        try {
            repo.setTier("com.game", Tier.TOOLS)
            fail("Expected RepositoryException")
        } catch (_: RepositoryException) {
        }
    }

    @Test
    fun movingOutOfGatedWithAuthSucceeds() = runBlocking {
        val dao = FakeAppEntryDao(
            listOf(AppEntryEntity("com.game", Tier.GATED.name, 0, null)),
        )
        val gate = FakeGateSession(applyAuthorized = true)
        val repo = buildRepo(dao, gate)
        repo.setTier("com.game", Tier.TOOLS)
        assertEquals(Tier.TOOLS.name, dao.get("com.game")?.tier)
    }

    private fun buildRepo(dao: FakeAppEntryDao, gate: FakeGateSession): AppRepository {
        val slots = FakeSlotOverrideDao()
        val launch = FakeLaunchOverrideDao()
        return AppRepository(
            restoreDefaultsGateway = FakeRestoreDefaultsGateway(dao, slots, launch),
            appEntryDao = dao,
            slotOverrideDao = slots,
            launchOverrideDao = launch,
            firstRunStore = FakeFirstRunPreferences(),
            slotResolver = FakeSlotResolverReader(),
            appCatalog = FakeAppCatalogReader(),
            packagePresence = FakePackagePresenceChecker(),
            gateSession = gate,
            ownPackageName = "app.plain.launcher",
        )
    }
}
