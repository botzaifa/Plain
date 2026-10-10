package app.plain.launcher.gate

import androidx.lifecycle.ViewModel
import app.plain.launcher.data.GateRepository

class GateViewModel(
    private val gateRepository: GateRepository,
    val gateSession: GateSessionController,
) : ViewModel() {
    suspend fun waitSecondsForToday(): Int {
        return GatePolicy.waitSeconds(gateRepository.passesToday())
    }
}
