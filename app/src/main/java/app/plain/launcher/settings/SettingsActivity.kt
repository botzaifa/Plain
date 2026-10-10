package app.plain.launcher.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import app.plain.launcher.BuildConfig
import app.plain.launcher.PlainApp
import app.plain.launcher.R
import app.plain.launcher.auth.RecoveryCode
import app.plain.launcher.core.EdgeInsets
import app.plain.launcher.databinding.ActivitySettingsBinding
import app.plain.launcher.enforce.A11yHealth
import app.plain.launcher.enforce.A11yStatus
import app.plain.launcher.enforce.GateAccessibilityService
import app.plain.launcher.policy.BasicRules
import app.plain.launcher.system.HomeRole
import app.plain.launcher.unlock.RecoveryFlow
import java.text.DateFormat
import java.util.Date

class SettingsActivity : Activity() {
    private lateinit var binding: ActivitySettingsBinding
    private val app get() = PlainApp.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!app.adminSession.isActive()) {
            finish()
            return
        }
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EdgeInsets.applySystemBars(binding.settingsRoot)
        binding.btnBasic.setOnClickListener { startActivity(Intent(this, BasicPickerActivity::class.java)) }
        binding.btnPasswords.setOnClickListener { startActivity(Intent(this, ChangePasswordsActivity::class.java)) }
        binding.btnRecovery.setOnClickListener { regenerateRecovery() }
        binding.btnRestore.setOnClickListener { restoreDefaults() }
        binding.btnHome.setOnClickListener { HomeRole.launchRequest(this, REQ_HOME) }
        binding.btnA11y.setOnClickListener {
            app.adminSession.grantSystemSettingsPass()
            A11yStatus.openSettings(this)
        }
        binding.btnHomeSettings.setOnClickListener {
            app.adminSession.grantSystemSettingsPass()
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }
        binding.btnAppInfo.setOnClickListener {
            app.adminSession.grantSystemSettingsPass()
            val intent =
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
            startActivity(intent)
        }
        refreshStatus()
    }

    override fun onResume() {
        super.onResume()
        if (!app.adminSession.isActive()) {
            finish()
            return
        }
        refreshStatus()
    }

    private fun refreshStatus() {
        val home = if (HomeRole.isHomeHeld(this)) "yes" else "no"
        val a11y =
            when (A11yStatus.health(this)) {
                A11yHealth.RUNNING -> "running"
                A11yHealth.OFF -> "off"
                A11yHealth.ENABLED_BUT_DEAD -> "enabled, not running"
            }
        binding.statusText.text = "Home: $home\nAccessibility: $a11y\nVersion ${BuildConfig.VERSION_NAME}"
        val tripTime = app.store.getLong(GateAccessibilityService.KEY_LOOP_TRIP_TIME, 0L)
        val tripPkg = app.store.getString(GateAccessibilityService.KEY_LOOP_TRIP_PKG, null)
        if (tripTime > 0L) {
            val whenStr = DateFormat.getDateTimeInstance().format(Date(tripTime))
            binding.warningText.visibility = View.VISIBLE
            binding.warningText.text = getString(R.string.loop_guard_warning, whenStr, tripPkg ?: "?")
        } else {
            binding.warningText.visibility = View.GONE
        }
        warnExtraHomeLaunchers()
    }

    private fun warnExtraHomeLaunchers() {
        val homePkgs =
            packageManager.queryIntentActivities(
                Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
                0,
            ).mapNotNull { it.activityInfo?.packageName }.toSet()
        val launchable = app.catalog.launchablePackages()
        val extras = homePkgs.filter { it != BuildConfig.APPLICATION_ID && launchable.contains(it) }
        if (extras.isNotEmpty()) {
            binding.warningText.visibility = View.VISIBLE
            binding.warningText.append("\nExtra HOME apps (always allowed): ${extras.joinToString()}")
        }
    }

    private fun restoreDefaults() {
        val launchable = app.catalog.launchablePackages()
        val defs = app.defaults.resolveAll(launchable)
        val dialer = app.defaults.dialer()
        app.basicStore.setOrdered(BasicRules.ensureDialerPresent(defs, dialer))
        Toast.makeText(this, "Restored", Toast.LENGTH_SHORT).show()
    }

    private fun regenerateRecovery() {
        val code = RecoveryCode.generate()
        app.auth.setRecoveryCode(code)
        RecoveryFlow.showNewCodeOnce(this, code) {}
    }

    companion object {
        private const val REQ_HOME = 42

        fun intent(context: Context): Intent = Intent(context, SettingsActivity::class.java)
    }
}
