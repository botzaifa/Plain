package app.plain.launcher.fakes

import app.plain.launcher.data.AppEntryEntity
import app.plain.launcher.data.RestoreDefaultsGateway

class FakeRestoreDefaultsGateway(
    private val appEntryDao: FakeAppEntryDao,
    private val slotOverrideDao: FakeSlotOverrideDao,
    private val launchOverrideDao: FakeLaunchOverrideDao,
) : RestoreDefaultsGateway {
    override suspend fun restoreDefaultsInTransaction(planned: List<AppEntryEntity>) {
        appEntryDao.deleteAll()
        slotOverrideDao.deleteAll()
        launchOverrideDao.deleteAll()
        appEntryDao.upsertAll(planned)
    }
}
