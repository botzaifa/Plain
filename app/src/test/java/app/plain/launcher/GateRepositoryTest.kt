package app.plain.launcher

import app.plain.launcher.data.GatePassDao
import app.plain.launcher.data.GatePassEntity
import app.plain.launcher.data.GateRepository
import app.plain.launcher.fakes.FakeClock
import app.plain.launcher.gate.GatePolicy
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class GateRepositoryTest {
    @Test
    fun recordPassIncrementsForToday() = runBlocking {
        val clock = FakeClock(Instant.parse("2026-10-10T12:00:00Z"), ZoneId.of("UTC"))
        val dao = InMemoryGatePassDao()
        val repo = GateRepository(dao, clock)
        assertEquals(0, repo.passesToday())
        repo.recordSuccessfulPass()
        assertEquals(1, repo.passesToday())
    }

    @Test
    fun midnightChangesPassCount() = runBlocking {
        val clock = FakeClock(Instant.parse("2026-10-10T23:59:00Z"), ZoneId.of("UTC"))
        val dao = InMemoryGatePassDao()
        val repo = GateRepository(dao, clock)
        repo.recordSuccessfulPass()
        clock.setInstant(Instant.parse("2026-10-11T00:01:00Z"))
        assertEquals(0, repo.passesToday())
        assertEquals("2026-10-11", GatePolicy.todayKey(clock))
    }
}

private class InMemoryGatePassDao : GatePassDao {
    private val rows = mutableMapOf<String, GatePassEntity>()

    override suspend fun get(date: String): GatePassEntity? = rows[date]

    override suspend fun recent(days: Int): List<GatePassEntity> =
        rows.values.sortedByDescending { it.date }.take(days)

    override suspend fun upsert(entity: GatePassEntity) {
        rows[entity.date] = entity
    }

    override suspend fun deleteOlderThan(beforeDate: String) {
        rows.keys.filter { it < beforeDate }.forEach { rows.remove(it) }
    }
}
