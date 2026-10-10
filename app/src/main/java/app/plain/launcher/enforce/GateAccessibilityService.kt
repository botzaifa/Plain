package app.plain.launcher.enforce

import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import app.plain.launcher.PlainApp
import app.plain.launcher.R
import app.plain.launcher.auth.UnlockState
import app.plain.launcher.core.Config
import app.plain.launcher.core.SettingsPackages
import app.plain.launcher.policy.EnforcementAction
import app.plain.launcher.policy.EnforcementPolicy
import app.plain.launcher.policy.LoopGuardDecision

class GateAccessibilityService : AccessibilityService() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tempWarnShown = false

    private val tickRunnable =
        object : Runnable {
            override fun run() {
                onTick()
                mainHandler.postDelayed(this, 1000L)
            }
        }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        lastConnectedElapsed = SystemClock.elapsedRealtime()
        PlainApp.instance.refreshAlwaysAllowed()
        mainHandler.post(tickRunnable)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(tickRunnable)
        if (instance === this) instance = null
        super.onDestroy()
    }

    override fun onInterrupt() {
        // no-op
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        try {
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> evaluate(event.packageName?.toString())
                AccessibilityEvent.TYPE_WINDOWS_CHANGED -> evaluatePictureInPicture()
            }
        } catch (_: Exception) {
            // Window queries can throw on some builds; avoid killing the service.
        }
    }

    private fun onTick() {
        try {
            val app = PlainApp.instance
            val now = app.clock.elapsedRealtime()
            if (app.auth.stateMachine.expireIfNeeded(now)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
            }
            val state = app.auth.stateMachine.effectiveState(now)
            if (state == UnlockState.TEMP_UNLOCKED &&
                app.auth.stateMachine.shouldWarn(now, Config.WARNING_BEFORE_MS) &&
                !tempWarnShown
            ) {
                tempWarnShown = true
                Toast.makeText(this, R.string.temp_warning, Toast.LENGTH_LONG).show()
            }
            if (state != UnlockState.TEMP_UNLOCKED) {
                tempWarnShown = false
            }
        } catch (_: Exception) {
            // Keep the service alive if app state is temporarily unavailable.
        }
    }

    private fun evaluatePictureInPicture() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) return
        val snapshot =
            try {
                windows?.toList().orEmpty()
            } catch (_: Exception) {
                return
            }
        for (window in snapshot) {
            if (!window.isInPictureInPictureMode) continue
            val root =
                try {
                    window.root
                } catch (_: Exception) {
                    null
                }
            val pkg = root?.packageName?.toString() ?: continue
            if (shouldBlock(pkg)) {
                dismissPictureInPicture()
                return
            }
        }
    }

    private fun shouldBlock(foregroundPkg: String?): Boolean {
        if (foregroundPkg.isNullOrBlank()) return false
        val app = PlainApp.instance
        val now = app.clock.elapsedRealtime()
        app.auth.stateMachine.expireIfNeeded(now)
        if (now < app.enforcementPausedUntil) return false
        val action =
            EnforcementPolicy.decide(
                foregroundPkg = foregroundPkg,
                state = app.auth.stateMachine.effectiveState(now),
                launchable = app.catalog.launchablePackages(),
                basic = app.basicStore.ensureWithDialer(app.defaults.dialer(), app.catalog.launchablePackages()).toSet(),
                alwaysAllowed = app.alwaysAllowedCache,
                adminSettingsPassActive = app.adminSession.isSystemSettingsPassActive(now),
                enforcementEnabled = app.auth.isSetupComplete(),
                enforcementPaused = now < app.enforcementPausedUntil,
                allowSettingsUntilA11yOn =
                    app.auth.isSetupComplete() && instance == null,
                settingsPackages = SettingsPackages.known,
            )
        return action == EnforcementAction.SEND_HOME
    }

    private fun dismissPictureInPicture() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            @Suppress("WrongConstant")
            performGlobalAction(GLOBAL_ACTION_DISMISS_PICTURE_IN_PICTURE)
        } else {
            performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    private fun evaluate(foregroundPkg: String?) {
        if (!shouldBlock(foregroundPkg)) return
        val app = PlainApp.instance
        val now = app.clock.elapsedRealtime()

        when (app.loopGuard.onSendHome(now)) {
            LoopGuardDecision.PAUSED -> {
                app.enforcementPausedUntil = app.loopGuard.pauseUntil(now)
                app.lastLoopGuardTrip = PlainApp.LoopGuardTrip(now, foregroundPkg)
                app.store.putLong(KEY_LOOP_TRIP_TIME, now)
                app.store.putString(KEY_LOOP_TRIP_PKG, foregroundPkg)
            }
            LoopGuardDecision.ALLOW_SEND -> performGlobalAction(GLOBAL_ACTION_HOME)
        }
    }

    companion object {
        /** [AccessibilityService.performGlobalAction] — API 31+, not in older compile stubs. */
        private const val GLOBAL_ACTION_DISMISS_PICTURE_IN_PICTURE = 14

        @Volatile
        var instance: GateAccessibilityService? = null
            private set

        @Volatile
        var lastConnectedElapsed: Long = 0L
            private set

        const val KEY_LOOP_TRIP_TIME = "loop_trip_time"
        const val KEY_LOOP_TRIP_PKG = "loop_trip_pkg"
    }
}
