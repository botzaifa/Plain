package app.plain.launcher.setup

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import app.plain.launcher.core.EdgeInsets
import androidx.recyclerview.widget.LinearLayoutManager
import app.plain.launcher.PlainApp
import app.plain.launcher.R
import app.plain.launcher.auth.RecoveryCode
import app.plain.launcher.databinding.ActivityWizardBinding
import app.plain.launcher.home.HomeActivity
import app.plain.launcher.policy.BasicRules
import app.plain.launcher.settings.BasicMoveCallback
import androidx.recyclerview.widget.ItemTouchHelper
import app.plain.launcher.settings.BasicPickerAdapter

class WizardActivity : Activity() {
    private lateinit var binding: ActivityWizardBinding
    private val app get() = PlainApp.instance
    private var pendingRecoveryCode: String? = null
    private var basicAdapter: BasicPickerAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (app.auth.isSetupComplete()) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }
        binding = ActivityWizardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupInsets()
        wireWelcome()
        wirePasswords()
        wireRecovery()
        wireBasic()
        wireDone()
        setupPasswordScrollInsets()
    }

    private fun setupPasswordScrollInsets() {
        EdgeInsets.applyImePaddingBottom(findViewById(R.id.wizardPasswordsScroll))
    }

    private fun wireWelcome() {
        findViewById<Button>(R.id.welcomeNext).setOnClickListener {
            binding.flipper.showNext()
        }
    }

    private fun wirePasswords() {
        findViewById<Button>(R.id.passwordsNext).setOnClickListener {
            val t1 = findViewById<EditText>(R.id.wTemp1).text.toString()
            val t2 = findViewById<EditText>(R.id.wTemp2).text.toString()
            val f1 = findViewById<EditText>(R.id.wFull1).text.toString()
            val f2 = findViewById<EditText>(R.id.wFull2).text.toString()
            val a1 = findViewById<EditText>(R.id.wAdmin1).text.toString()
            val a2 = findViewById<EditText>(R.id.wAdmin2).text.toString()
            if (t1 != t2 || f1 != f2 || a1 != a2) {
                Toast.makeText(this, "Confirmation mismatch", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val t = t1.toCharArray()
            val f = f1.toCharArray()
            val a = a1.toCharArray()
            if (!app.auth.validateLengths(t, f, a) || !app.auth.validateDistinct(t, f, a)) {
                Toast.makeText(this, "Check lengths and distinct passwords", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val progress = ProgressDialog.show(this, null, "Loading…", true, false)
            app.backgroundExecutor.execute {
                val iter = app.auth.calibrateAndStoreIterations()
                app.auth.setPasswords(t, f, a, iter)
                pendingRecoveryCode = RecoveryCode.generate()
                app.auth.setRecoveryCode(pendingRecoveryCode!!)
                runOnUiThread {
                    progress.dismiss()
                    showRecoveryStep()
                    binding.flipper.showNext()
                }
            }
        }
    }

    private fun showRecoveryStep() {
        val code = pendingRecoveryCode ?: return
        findViewById<TextView>(R.id.recoveryCodeDisplay).text = RecoveryCode.format(code)
        val confirm = findViewById<CheckBox>(R.id.recoveryConfirm)
        val next = findViewById<Button>(R.id.recoveryNext)
        confirm.isChecked = false
        updateRecoveryNextEnabled(next, false)
        confirm.setOnCheckedChangeListener { _, checked -> updateRecoveryNextEnabled(next, checked) }
    }

    private fun updateRecoveryNextEnabled(next: Button, enabled: Boolean) {
        next.isEnabled = enabled
        next.alpha = if (enabled) 1f else 0.35f
    }

    private fun wireRecovery() {
        val next = findViewById<Button>(R.id.recoveryNext)
        updateRecoveryNextEnabled(next, false)
        next.setOnClickListener {
            if (!findViewById<CheckBox>(R.id.recoveryConfirm).isChecked) return@setOnClickListener
            binding.flipper.showNext()
            setupBasicStep()
        }
    }

    private fun setupBasicStep() {
        val list = findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.wizardBasicList)
        val launchable = app.catalog.launchablePackages()
        val defs = app.defaults.resolveAll(launchable)
        val dialer = app.defaults.dialer()
        val planned = BasicRules.ensureDialerPresent(defs, dialer)
        val all = app.catalog.getLaunchable()
        val set = planned.toSet()
        val rows =
            planned.mapNotNull { pkg -> all.find { it.packageName == pkg }?.let { BasicPickerAdapter.Row(it, true) } } +
                all.filter { !set.contains(it.packageName) }.sortedBy { it.sortKey }
                    .map { BasicPickerAdapter.Row(it, false) }
        basicAdapter = BasicPickerAdapter(dialer)
        list.layoutManager = LinearLayoutManager(this)
        list.itemAnimator = BasicPickerAdapter.createItemAnimator()
        list.adapter = basicAdapter
        basicAdapter?.setData(rows)
        basicAdapter?.let {
            ItemTouchHelper(BasicMoveCallback(it)).attachToRecyclerView(list)
        }
    }

    private fun wireBasic() {
        findViewById<Button>(R.id.basicNext).setOnClickListener {
            basicAdapter?.let { app.basicStore.setOrdered(it.orderedSelected()) }
            binding.flipper.showNext()
        }
    }

    private fun wireDone() {
        findViewById<Button>(R.id.doneFinish).setOnClickListener {
            app.auth.markSetupComplete()
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }

    private fun setupInsets() {
        EdgeInsets.applySystemBars(binding.root)
    }

}
