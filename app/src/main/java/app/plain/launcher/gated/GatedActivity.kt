package app.plain.launcher.gated

import android.app.Activity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import app.plain.launcher.PlainApp
import app.plain.launcher.apps.Launcher
import app.plain.launcher.auth.UnlockState
import app.plain.launcher.core.EdgeInsets
import app.plain.launcher.databinding.ActivityGatedBinding

class GatedActivity : Activity() {
    private lateinit var binding: ActivityGatedBinding
    private val app get() = PlainApp.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGatedBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EdgeInsets.applySystemBars(binding.gatedRoot)
        val iconPx = (48 * resources.displayMetrics.density).toInt()
        val adapter =
            GatedListAdapter(app.iconLoader, iconPx) { entry ->
                Launcher.launch(this, entry)
            }
        binding.list.layoutManager = LinearLayoutManager(this)
        binding.list.adapter = adapter
        adapter.submit(gatedApps())
    }

    override fun onResume() {
        super.onResume()
        if (app.auth.stateMachine.expireIfNeeded(app.clock.elapsedRealtime())) {
            finish()
            return
        }
        if (app.auth.stateMachine.effectiveState() == UnlockState.LOCKED) {
            finish()
        }
    }

    private fun gatedApps(): List<app.plain.launcher.apps.AppEntry> {
        val basic =
            app.basicStore.ensureWithDialer(app.defaults.dialer(), app.catalog.launchablePackages()).toSet()
        return app.catalog.getLaunchable().filter { !basic.contains(it.packageName) }
    }
}
