package app.plain.launcher

import app.plain.launcher.gate.GatePurpose
import app.plain.launcher.gate.GateSession
import app.plain.launcher.gate.GateSessionState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GateSessionTest {
    @Test
    fun countdownLaunchPurpose_authorizesOnce() {
        val session = GateSession()
        session.beginWait(GatePurpose.Launch, 15, 0L)
        session.onCountdownFinished()
        assertTrue(session.consumeLaunchAuthorization())
        assertFalse(session.consumeLaunchAuthorization())
    }

    @Test
    fun cancelResetsToIdle() {
        val session = GateSession()
        session.beginWait(GatePurpose.Apply, 15, 0L)
        session.cancel()
        assertTrue(session.state.value is GateSessionState.Idle)
    }
}
