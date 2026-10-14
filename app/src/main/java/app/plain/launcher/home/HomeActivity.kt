package app.plain.launcher.home

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.window.OnBackInvokedDispatcher
import android.view.View
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import app.plain.launcher.PlainApp
import app.plain.launcher.R
import app.plain.launcher.apps.AppCatalog
import app.plain.launcher.apps.AppEntry
import app.plain.launcher.apps.Launcher
import app.plain.launcher.auth.PasswordLevel
import app.plain.launcher.auth.UnlockState
import app.plain.launcher.databinding.ActivityHomeBinding
import app.plain.launcher.enforce.A11yHealth
import app.plain.launcher.enforce.A11ySetupFlow
import app.plain.launcher.enforce.A11yStatus
import app.plain.launcher.enforce.RestrictedSettingsHelper
import app.plain.launcher.gated.GatedActivity
import app.plain.launcher.settings.SettingsActivity
import app.plain.launcher.setup.WizardActivity
import app.plain.launcher.system.HomeRole
import app.plain.launcher.unlock.PasswordPrompt
import app.plain.launcher.unlock.RecoveryFlow
import app.plain.launcher.unlock.PromptMode
import app.plain.launcher.system.KeyguardHelper
import java.util.Locale
import java.util.concurrent.TimeUnit

class HomeActivity : Activity(), AppCatalog.Listener {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: HomeGridAdapter
    private val app get() = PlainApp.instance
    private var contentUnlocked = false
    private val tickHandler = Handler(Looper.getMainLooper())
    private val tickRunnable =
        object : Runnable {
            override fun run() {
                updateLockUi()
                tickHandler.postDelayed(this, 1000L)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!app.auth.isSetupComplete()) {
            startActivity(Intent(this, WizardActivity::class.java))
            finish()
            return
        }
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()
        setupBack()
        val iconPx = (72 * resources.displayMetrics.density).toInt()
        adapter =
            HomeGridAdapter(
                app.iconLoader,
                iconPx,
                ::launchBasic,
                ::onMoreClicked,
            )
        val widthDp = resources.configuration.screenWidthDp
        val spans = maxOf(2, widthDp / 160)
        binding.grid.layoutManager = GridLayoutManager(this, spans)
        binding.grid.adapter = adapter
        binding.btnLock.setOnClickListener {
            app.auth.stateMachine.lock()
            updateLockUi()
        }
        binding.clockContainer.setOnLongClickListener {
            PasswordPrompt.show(
                this,
                PromptMode.ADMIN_ONLY,
                onSuccess = {
                    if (it == PasswordLevel.ADMIN) {
                        app.adminSession.start()
                        startActivity(SettingsActivity.intent(this))
                    }
                },
                onRecovery = { RecoveryFlow.start(this) },
            )
            true
        }
        app.catalog.addListener(this)
        refreshGrid()
        updateWarnings()
    }

    override fun onDestroy() {
        app.catalog.removeListener(this)
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        binding.grid.scrollToPosition(0)
        app.adminSession.clearSystemSettingsPass()
    }

    override fun onResume() {
        super.onResume()
        if (!contentUnlocked && KeyguardHelper.isLocked(this)) {
            binding.root.visibility = View.INVISIBLE
            KeyguardHelper.whenUnlocked(
                this,
                onUnlocked = {
                    contentUnlocked = true
                    binding.root.visibility = View.VISIBLE
                    resumeHome()
                },
            )
            return
        }
        contentUnlocked = !KeyguardHelper.isLocked(this)
        binding.root.visibility = View.VISIBLE
        resumeHome()
    }

    private fun resumeHome() {
        val now = app.clock.elapsedRealtime()
        if (app.auth.stateMachine.expireIfNeeded(now)) {
            Toast.makeText(this, R.string.temp_warning, Toast.LENGTH_SHORT).show()
        }
        updateLockUi()
        updateWarnings()
        tickHandler.post(tickRunnable)
        app.catalog.refresh()
    }

    override fun onPause() {
        tickHandler.removeCallbacks(tickRunnable)
        if (KeyguardHelper.isLocked(this)) {
            contentUnlocked = false
        }
        super.onPause()
    }

    override fun onCatalogChanged() {
        binding.grid.post { refreshGrid() }
    }

    private fun refreshGrid() {
        val launchable = app.catalog.launchablePackages()
        val dialer = app.defaults.dialer()
        val order = app.basicStore.ensureWithDialer(dialer, launchable)
        val byPkg = app.catalog.getLaunchable().associateBy { it.packageName }
        val apps = order.mapNotNull { byPkg[it] }
        adapter.submit(apps)
    }

    private fun launchBasic(entry: AppEntry) {
        Launcher.launch(this, entry)
    }

    private fun onMoreClicked() {
        val state = app.auth.stateMachine.effectiveState()
        if (state == UnlockState.LOCKED) {
            PasswordPrompt.show(
                this,
                PromptMode.GATED,
                onSuccess = { level -> applyUnlockAndOpenGated(level) },
            )
        } else {
            startActivity(Intent(this, GatedActivity::class.java))
        }
    }

    private fun applyUnlockAndOpenGated(level: PasswordLevel) {
        when (level) {
            PasswordLevel.TEMP -> app.auth.stateMachine.unlockTemp()
            PasswordLevel.FULL, PasswordLevel.ADMIN -> app.auth.stateMachine.unlockFull()
            PasswordLevel.NONE -> return
        }
        updateLockUi()
        startActivity(Intent(this, GatedActivity::class.java))
    }

    private fun updateLockUi() {
        val state = app.auth.stateMachine.effectiveState()
        if (state == UnlockState.LOCKED) {
            binding.btnLock.visibility = View.GONE
            return
        }
        binding.btnLock.visibility = View.VISIBLE
        if (state == UnlockState.TEMP_UNLOCKED) {
            val rem = app.auth.stateMachine.remainingMs()
            val mins = TimeUnit.MILLISECONDS.toMinutes(rem)
            val secs = TimeUnit.MILLISECONDS.toSeconds(rem) % 60
            binding.btnLock.text = getString(R.string.lock_with_time, String.format(Locale.getDefault(), "%d:%02d", mins, secs))
        } else {
            binding.btnLock.setText(R.string.lock)
        }
    }

    private fun updateWarnings() {
        if (!HomeRole.isHomeHeld(this)) {
            showSetupNotice(
                titleRes = R.string.home_notice_title,
                bodyRes = R.string.home_notice_body,
                actionRes = R.string.home_notice_action,
            ) {
                HomeRole.launchRequest(this, REQ_HOME)
            }
            return
        }
        when (A11yStatus.health(this)) {
            A11yHealth.OFF -> {
                if (RestrictedSettingsHelper.needsAllowRestrictedSettings(this)) {
                    showSetupNotice(
                        titleRes = R.string.a11y_restricted_notice_title,
                        bodyRes = R.string.a11y_restricted_notice_body,
                        actionRes = R.string.a11y_restricted_notice_action,
                    ) {
                        app.adminSession.grantSystemSettingsPass()
                        A11ySetupFlow.start(this)
                    }
                } else {
                    showSetupNotice(
                        titleRes = R.string.a11y_off_notice_title,
                        bodyRes = R.string.a11y_off_notice_body,
                        actionRes = R.string.a11y_off_notice_action,
                    ) {
                        app.adminSession.grantSystemSettingsPass()
                        A11ySetupFlow.start(this)
                    }
                }
            }
            A11yHealth.ENABLED_BUT_DEAD -> {
                showSetupNotice(
                    titleRes = R.string.a11y_dead_notice_title,
                    bodyRes = R.string.a11y_dead_notice_body,
                    actionRes = R.string.a11y_dead_notice_action,
                ) {
                    app.adminSession.grantSystemSettingsPass()
                    A11ySetupFlow.start(this)
                }
            }
            A11yHealth.RUNNING -> hideSetupNotice()
        }
    }

    private fun showSetupNotice(
        titleRes: Int,
        bodyRes: Int,
        actionRes: Int,
        onAction: () -> Unit,
    ) {
        val notice = binding.setupNoticeInclude
        notice.setupNotice.visibility = View.VISIBLE
        notice.setupNoticeTitle.setText(titleRes)
        notice.setupNoticeBody.setText(bodyRes)
        notice.setupNoticeAction.setText(actionRes)
        notice.setupNoticeAction.setOnClickListener { onAction() }
    }

    private fun hideSetupNotice() {
        val notice = binding.setupNoticeInclude
        notice.setupNotice.visibility = View.GONE
        notice.setupNoticeAction.setOnClickListener(null)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_HOME) {
            updateWarnings()
        }
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // no-op
    }

    private fun setupBack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT) {
                // no-op
            }
        }
    }

    companion object {
        private const val REQ_HOME = 42
    }
}
