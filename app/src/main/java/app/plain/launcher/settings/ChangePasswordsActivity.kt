package app.plain.launcher.settings

import android.app.Activity
import android.app.ProgressDialog
import android.os.Bundle
import android.widget.Toast
import app.plain.launcher.PlainApp
import app.plain.launcher.core.EdgeInsets
import app.plain.launcher.databinding.ActivityChangePasswordsBinding

class ChangePasswordsActivity : Activity() {
    private lateinit var binding: ActivityChangePasswordsBinding
    private val app get() = PlainApp.instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        EdgeInsets.applySystemBars(binding.changePasswordsRoot)
        binding.btnSave.setOnClickListener { save() }
    }

    private fun save() {
        val temp = binding.temp1.text.toString()
        val temp2 = binding.temp2.text.toString()
        val full = binding.full1.text.toString()
        val full2 = binding.full2.text.toString()
        val admin = binding.admin1.text.toString()
        val admin2 = binding.admin2.text.toString()
        if (temp != temp2 || full != full2 || admin != admin2) {
            Toast.makeText(this, "Confirmation mismatch", Toast.LENGTH_SHORT).show()
            return
        }
        val t = temp.toCharArray()
        val f = full.toCharArray()
        val a = admin.toCharArray()
        if (!app.auth.validateLengths(t, f, a) || !app.auth.validateDistinct(t, f, a)) {
            Toast.makeText(this, "Check lengths and distinct passwords", Toast.LENGTH_SHORT).show()
            return
        }
        val progress = ProgressDialog.show(this, null, "Loading…", true, false)
        app.backgroundExecutor.execute {
            val iter = app.auth.storedIterations()
            app.auth.setPasswords(t, f, a, iter)
            runOnUiThread {
                progress.dismiss()
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
