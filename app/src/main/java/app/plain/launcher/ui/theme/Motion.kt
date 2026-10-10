package app.plain.launcher.ui.theme

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

object PlainMotion {
    const val ShortMs = 150
    const val MediumMs = 200
    const val LongMs = 250

    fun tweenShort() = tween<Float>(ShortMs, easing = FastOutSlowInEasing)
    fun tweenMedium() = tween<Float>(MediumMs, easing = FastOutSlowInEasing)
    fun tweenLong() = tween<Float>(LongMs, easing = FastOutSlowInEasing)
}
