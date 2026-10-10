package app.plain.launcher

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import app.plain.launcher.apps.Tier
import app.plain.launcher.data.AppEntryEntity
import app.plain.launcher.data.GatePassEntity
import app.plain.launcher.data.LaunchOverrideEntity
import app.plain.launcher.data.PlainDatabase
import app.plain.launcher.data.SlotOverrideEntity
import java.sql.SQLException
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RestoreDefaultsTransactionTest {
    private lateinit var db: PlainDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, PlainDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun restoreDefaultsRollsBackAllTablesOnFailure() = runBlocking {
        seedCustomState()
        val before = snapshot()

        try {
            db.withTransaction {
                db.appEntryDao().deleteAll()
                db.slotOverrideDao().deleteAll()
                db.launchOverrideDao().deleteAll()
                throw SQLException("forced failure before upsert")
            }
        } catch (_: SQLException) {
        }

        assertEquals(before, snapshot())
    }

    @Test
    fun restoreDefaultsTransactionReplacesTiersAndOverrides() = runBlocking {
        seedCustomState()
        db.gatePassDao().upsert(GatePassEntity("2026-01-01", 3))
        val gateBefore = db.gatePassDao().get("2026-01-01")

        val planned = listOf(
            AppEntryEntity("com.phone", Tier.HOME.name, 0, null),
            AppEntryEntity("com.game", Tier.GATED.name, 0, null),
        )
        db.restoreDefaultsInTransaction(planned)

        assertEquals(planned, db.appEntryDao().getAll())
        assertEquals(emptyList<SlotOverrideEntity>(), db.slotOverrideDao().getAll())
        assertEquals(emptyList<LaunchOverrideEntity>(), db.launchOverrideDao().getAll())
        assertEquals(gateBefore, db.gatePassDao().get("2026-01-01"))
    }

    private suspend fun seedCustomState() {
        db.appEntryDao().upsert(AppEntryEntity("com.custom", Tier.TOOLS.name, 2, null))
        db.slotOverrideDao().upsert(SlotOverrideEntity("Photos", "com.gallery"))
        db.launchOverrideDao().upsert(LaunchOverrideEntity("com.custom", "com.custom.Main"))
    }

    private suspend fun snapshot(): DbSnapshot {
        return DbSnapshot(
            apps = db.appEntryDao().getAll(),
            slots = db.slotOverrideDao().getAll(),
            launches = db.launchOverrideDao().getAll(),
        )
    }

    private data class DbSnapshot(
        val apps: List<AppEntryEntity>,
        val slots: List<SlotOverrideEntity>,
        val launches: List<LaunchOverrideEntity>,
    )
}
