package app.plain.launcher.gate

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class GatePurpose {
    Launch,
    Apply,
}

sealed interface GateSessionState {
    data object Idle : GateSessionState
    data class Waiting(
        val purpose: GatePurpose,
        val totalSeconds: Int,
        val startedAtElapsedMs: Long,
    ) : GateSessionState

    data object LaunchAuthorized : GateSessionState
    data object ApplyAuthorized : GateSessionState
}

interface GateSessionController {
    val state: StateFlow<GateSessionState>
    fun beginWait(purpose: GatePurpose, totalSeconds: Int, elapsedRealtimeMs: Long)
    fun cancel()
    fun onCountdownFinished()
    fun onForegroundLost()
    fun consumeLaunchAuthorization(): Boolean
    fun consumeApplyAuthorization(): Boolean
    fun hasApplyAuthorization(): Boolean
    fun hasLaunchAuthorization(): Boolean
}

class GateSession : GateSessionController {
    private val _state = MutableStateFlow<GateSessionState>(GateSessionState.Idle)
    override val state: StateFlow<GateSessionState> = _state.asStateFlow()

    override fun beginWait(purpose: GatePurpose, totalSeconds: Int, elapsedRealtimeMs: Long) {
        _state.value = GateSessionState.Waiting(purpose, totalSeconds, elapsedRealtimeMs)
    }

    override fun cancel() {
        _state.value = GateSessionState.Idle
    }

    override fun onCountdownFinished() {
        val waiting = _state.value as? GateSessionState.Waiting ?: return
        _state.value = when (waiting.purpose) {
            GatePurpose.Launch -> GateSessionState.LaunchAuthorized
            GatePurpose.Apply -> GateSessionState.ApplyAuthorized
        }
    }

    override fun onForegroundLost() {
        when (_state.value) {
            is GateSessionState.Waiting,
            is GateSessionState.LaunchAuthorized,
            is GateSessionState.ApplyAuthorized,
            -> _state.value = GateSessionState.Idle
            GateSessionState.Idle -> Unit
        }
    }

    override fun consumeLaunchAuthorization(): Boolean {
        if (_state.value !is GateSessionState.LaunchAuthorized) return false
        _state.value = GateSessionState.Idle
        return true
    }

    override fun consumeApplyAuthorization(): Boolean {
        if (_state.value !is GateSessionState.ApplyAuthorized) return false
        _state.value = GateSessionState.Idle
        return true
    }

    override fun hasApplyAuthorization(): Boolean = _state.value is GateSessionState.ApplyAuthorized

    override fun hasLaunchAuthorization(): Boolean = _state.value is GateSessionState.LaunchAuthorized
}
