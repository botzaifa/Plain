package app.plain.launcher.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import android.text.format.DateFormat
import java.util.Date
import java.util.Locale

data class TimeDisplay(
    val timeText: String,
    val dateText: String,
)

class TimeSource(private val context: Context) {
    fun observe(): Flow<TimeDisplay> = callbackFlow {
        fun emitNow() {
            trySend(formatNow())
        }
        emitNow()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                emitNow()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
            addAction(Intent.ACTION_LOCALE_CHANGED)
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)

        val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) {
                emitNow()
            }
        }
        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.TIME_12_24),
            false,
            observer,
        )

        awaitClose {
            context.unregisterReceiver(receiver)
            context.contentResolver.unregisterContentObserver(observer)
        }
    }

    private fun formatNow(): TimeDisplay {
        val now = Date()
        val locale = Locale.getDefault()
        val is24 = DateFormat.is24HourFormat(context)
        val timePattern = DateFormat.getBestDateTimePattern(locale, if (is24) "Hm" else "hm")
        val datePattern = DateFormat.getBestDateTimePattern(locale, "EEEEMMMd")
        val time = DateFormat.format(timePattern, now).toString()
        val date = DateFormat.format(datePattern, now).toString()
        return TimeDisplay(time, date)
    }
}
