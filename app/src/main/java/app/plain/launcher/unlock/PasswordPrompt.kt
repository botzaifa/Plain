package app.plain.launcher.unlock

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import app.plain.launcher.PlainApp
import app.plain.launcher.R
import app.plain.launcher.auth.PasswordLevel
enum class PromptMode {
    GATED,
    ADMIN_ONLY,
}

object PasswordPrompt {
    fun show(
        activity: Activity,
        mode: PromptMode,
        onSuccess: (PasswordLevel) -> Unit,
        onRecovery: (() -> Unit)? = null,
    ) {
        val app = PlainApp.instance
        val lock = app.auth.lockout.status()
        if (lock.locked) {
            showMessage(activity, activity.getString(R.string.locked_out, (lock.remainingMs / 1000).toInt()))
            return
        }

        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_password, null)
        val title = view.findViewById<TextView>(R.id.dialogTitle)
        val subtitle = view.findViewById<TextView>(R.id.dialogSubtitle)
        val input = view.findViewById<EditText>(R.id.passwordInput)
        val errorText = view.findViewById<TextView>(R.id.errorText)
        val btnForgot = view.findViewById<Button>(R.id.btnForgot)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirm)

        when (mode) {
            PromptMode.GATED -> {
                title.setText(R.string.enter_password)
                subtitle.visibility = View.GONE
                btnCancel.visibility = View.GONE
                btnForgot.visibility = View.GONE
                btnConfirm.layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    )
            }
            PromptMode.ADMIN_ONLY -> {
                title.setText(R.string.enter_admin_password)
                subtitle.setText(R.string.password_dialog_admin_subtitle)
                btnForgot.visibility = if (onRecovery != null) View.VISIBLE else View.GONE
            }
        }

        input.inputType =
            if (app.auth.allPasswordsDigitsOnly()) {
                InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
            } else {
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            }

        val dialog =
            AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
                .setView(view)
                .create()

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnForgot.setOnClickListener {
            dialog.dismiss()
            onRecovery?.invoke()
        }
        btnConfirm.setOnClickListener {
            errorText.visibility = View.GONE
            val raw = input.text?.toString()?.trim().orEmpty()
            if (raw.isEmpty()) {
                errorText.text = activity.getString(R.string.wrong_password)
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            val text = raw.toCharArray()
            val progress = ProgressDialog.show(activity, null, "Loading…", true, false)
            app.backgroundExecutor.execute {
                val level = app.auth.verifyPassword(text)
                val wrong = level == PasswordLevel.NONE
                val lockStatus =
                    if (wrong) {
                        app.auth.lockout.onWrongAttempt()
                    } else {
                        app.auth.lockout.onSuccess()
                        null
                    }
                activity.runOnUiThread {
                    progress.dismiss()
                    when {
                        wrong -> {
                            if (lockStatus!!.locked) {
                                dialog.dismiss()
                                show(activity, mode, onSuccess, onRecovery)
                            } else {
                                errorText.text = activity.getString(R.string.wrong_password)
                                errorText.visibility = View.VISIBLE
                            }
                        }
                        mode == PromptMode.ADMIN_ONLY && level != PasswordLevel.ADMIN -> {
                            errorText.text = activity.getString(R.string.wrong_password)
                            errorText.visibility = View.VISIBLE
                        }
                        mode == PromptMode.GATED && level == PasswordLevel.NONE -> {
                            errorText.text = activity.getString(R.string.wrong_password)
                            errorText.visibility = View.VISIBLE
                        }
                        else -> {
                            dialog.dismiss()
                            onSuccess(level)
                        }
                    }
                }
            }
        }
        dialog.show()
    }

    private fun showMessage(activity: Activity, message: String) {
        AlertDialog.Builder(activity, R.style.Theme_Plain_Dialog)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}
