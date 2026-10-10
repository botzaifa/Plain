package app.plain.launcher.policy

import app.plain.launcher.core.Config

enum class LoopGuardDecision {
    ALLOW_SEND,
    PAUSED,
}

class LoopGuard {
    private val sendTimes = ArrayDeque<Long>()

    fun onSendHome(now: Long): LoopGuardDecision {
        prune(now)
        sendTimes.addLast(now)
        if (sendTimes.size > Config.LOOP_GUARD_MAX_SENDS &&
            sendTimes.last() - sendTimes.first() <= Config.LOOP_GUARD_WINDOW_MS
        ) {
            return LoopGuardDecision.PAUSED
        }
        return LoopGuardDecision.ALLOW_SEND
    }

    fun pauseUntil(now: Long): Long = now + Config.LOOP_GUARD_PAUSE_MS

    private fun prune(now: Long) {
        while (sendTimes.isNotEmpty() && now - sendTimes.first() > Config.LOOP_GUARD_WINDOW_MS) {
            sendTimes.removeFirst()
        }
    }
}
