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

    /** Full-screen scroll form: status/nav bars plus keyboard inset on the bottom. */
    fun applySystemBarsAndIme(target: View) {
        ViewCompat.setOnApplyWindowInsetsListener(target) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val bottom = maxOf(bars.bottom, ime.bottom)
            v.setPadding(bars.left, bars.top, bars.right, bottom)
            insets
        }
        target.requestApplyInsets()
    }

    /** Extra bottom padding when the IME opens (view already inside a padded parent). */
    fun applyImePaddingBottom(target: View) {
        ViewCompat.setOnApplyWindowInsetsListener(target) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, ime.bottom)
            insets
        }
        target.requestApplyInsets()
    }
}
