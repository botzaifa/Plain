package app.plain.launcher.core

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object EdgeInsets {
    fun applySystemBars(target: View) {
        ViewCompat.setOnApplyWindowInsetsListener(target) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
    }
}
