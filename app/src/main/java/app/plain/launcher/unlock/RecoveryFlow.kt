package app.plain.launcher.unlock

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import app.plain.launcher.PlainApp
import app.plain.launcher.R
import app.plain.launcher.auth.RecoveryCode
import app.plain.launcher.core.Config
import app.plain.launcher.settings.SettingsActivity

object RecoveryFlow {
    fun start(activity: Activity) {
        promptCode(activity)
    }

    private fun promptCode(activity: Activity) {
        val app = PlainApp.instance
        val lock = app.auth.lockout.status()
        if (lock.locked) {
            AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                .setMessage(activity.getString(R.string.locked_out, (lock.remainingMs / 1000).toInt()))
                .setPositiveButton(android.R.string.ok, null)
                .show()
            return
        }
        showSingleInput(
            activity,
            title = activity.getString(R.string.enter_recovery_code),
            subtitle = activity.getString(R.string.recovery_code_dialog_subtitle),
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS,
        ) { raw ->
            verifyCode(activity, raw)
        }
    }

    private fun verifyCode(activity: Activity, raw: String) {
        val app = PlainApp.instance
        val progress = ProgressDialog.show(activity, null, "Loading…", true, false)
        app.backgroundExecutor.execute {
            val ok = app.auth.verifyRecoveryCode(raw)
            val lockResult = if (!ok) app.auth.lockout.onWrongAttempt() else null
            if (ok) app.auth.lockout.onSuccess()
            activity.runOnUiThread {
                progress.dismiss()
                if (!ok) {
                    if (lockResult!!.locked) {
                        promptCode(activity)
                    } else {
                        AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                            .setMessage(R.string.wrong_password)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                    }
                    return@runOnUiThread
                }
                promptNewAdmin(activity)
            }
        }
    }

    private fun promptNewAdmin(activity: Activity) {
        val app = PlainApp.instance
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_admin_password_recovery, null)
        view.findViewById<TextView>(R.id.dialogTitle).setText(R.string.new_admin_password)
        view.findViewById<TextView>(R.id.dialogSubtitle).setText(R.string.password_admin_hint)
        val admin1 = view.findViewById<EditText>(R.id.admin1)
        val admin2 = view.findViewById<EditText>(R.id.admin2)
        val inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        admin1.inputType = inputType
        admin2.inputType = inputType
        val dialog =
            AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                .setView(view)
                .create()
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            val a1 = admin1.text?.toString().orEmpty()
            val a2 = admin2.text?.toString().orEmpty()
            if (a1 != a2) {
                AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                    .setMessage(R.string.password_confirm_mismatch)
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                return@setOnClickListener
            }
            val pwd = a1.toCharArray()
            if (pwd.size < Config.MIN_LEN_ADMIN) {
                AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                    .setMessage(activity.getString(R.string.password_admin_hint))
                    .setPositiveButton(android.R.string.ok, null)
                    .show()
                return@setOnClickListener
            }
            dialog.dismiss()
            val progress = ProgressDialog.show(activity, null, "Loading…", true, false)
            app.backgroundExecutor.execute {
                val distinct = app.auth.validateNewAdminDistinct(pwd)
                activity.runOnUiThread {
                    progress.dismiss()
                    if (!distinct) {
                        AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                            .setMessage(R.string.recovery_admin_must_differ)
                            .setPositiveButton(android.R.string.ok, null)
                            .show()
                        return@runOnUiThread
                    }
                    finishRecovery(activity, pwd)
                }
            }
        }
        dialog.show()
    }

    private fun finishRecovery(activity: Activity, admin: CharArray) {
        val app = PlainApp.instance
        app.auth.setAdminPassword(admin)
        val newCode = RecoveryCode.generate()
        app.auth.setRecoveryCode(newCode)
        showNewCodeOnce(activity, newCode) {
            app.adminSession.start()
            activity.startActivity(SettingsActivity.intent(activity))
        }
    }

    fun showNewCodeOnce(activity: Activity, code: String, onContinue: () -> Unit) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_recovery_code, null)
        view.findViewById<TextView>(R.id.codeDisplay).text = RecoveryCode.format(code)
        val confirm = view.findViewById<CheckBox>(R.id.confirmWritten)
        val btnContinue = view.findViewById<Button>(R.id.btnContinue)
        confirm.setOnCheckedChangeListener { _, checked -> btnContinue.isEnabled = checked }
        val dialog =
            AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                .setView(view)
                .create()
        btnContinue.setOnClickListener {
            dialog.dismiss()
            onContinue()
        }
        dialog.show()
    }

    private fun showSingleInput(
        activity: Activity,
        title: String,
        subtitle: String?,
        inputType: Int,
        onConfirm: (String) -> Unit,
    ) {
        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_single_input, null)
        view.findViewById<TextView>(R.id.dialogTitle).text = title
        val sub = view.findViewById<TextView>(R.id.dialogSubtitle)
        if (subtitle.isNullOrBlank()) {
            sub.visibility = View.GONE
        } else {
            sub.visibility = View.VISIBLE
            sub.text = subtitle
        }
        val input = view.findViewById<EditText>(R.id.inputField)
        input.inputType = inputType
        val dialog =
            AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                .setView(view)
                .create()
        view.findViewById<Button>(R.id.btnCancel).setOnClickListener { dialog.dismiss() }
        view.findViewById<Button>(R.id.btnConfirm).setOnClickListener {
            dialog.dismiss()
            onConfirm(input.text?.toString() ?: "")
        }
        dialog.show()
    }
}
