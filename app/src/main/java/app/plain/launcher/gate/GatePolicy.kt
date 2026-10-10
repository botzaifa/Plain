package app.plain.launcher.gate

import java.time.Clock
import java.time.LocalDate

object GatePolicy {
    fun waitSeconds(passesToday: Int): Int {
        val raw = GateConfig.baseSeconds + GateConfig.stepSeconds * passesToday
        return minOf(raw, GateConfig.maxSeconds)
    }

    fun todayKey(clock: Clock): String {
        return LocalDate.now(clock).toString()
    }
}
