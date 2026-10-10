package app.plain.launcher.data

import app.plain.launcher.gate.GateConfig
import app.plain.launcher.gate.GatePolicy
import java.time.Clock
import java.time.LocalDate

class GateRepository(
    private val gatePassDao: GatePassDao,
    private val clock: Clock,
) {
    suspend fun passesToday(): Int {
        val key = GatePolicy.todayKey(clock)
        return gatePassDao.get(key)?.count ?: 0
    }

    suspend fun recordSuccessfulPass() {
        val key = GatePolicy.todayKey(clock)
        val current = gatePassDao.get(key)?.count ?: 0
        gatePassDao.upsert(GatePassEntity(date = key, count = current + 1))
        pruneOld()
    }

    suspend fun historyLastDays(days: Int = GateConfig.historyDays): List<GatePassEntity> {
        return gatePassDao.recent(days)
    }

    private suspend fun pruneOld() {
        val cutoff = LocalDate.now(clock).minusDays(GateConfig.retentionDays.toLong()).toString()
        gatePassDao.deleteOlderThan(cutoff)
    }
}
