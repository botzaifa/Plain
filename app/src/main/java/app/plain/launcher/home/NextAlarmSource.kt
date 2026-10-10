package app.plain.launcher.home

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.text.format.DateFormat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

class NextAlarmSource(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun observe(): Flow<String?> = callbackFlow {
        fun emitNow() {
            trySend(formatNext())
        }
        emitNow()

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                emitNow()
            }
        }
        context.registerReceiver(
            receiver,
            IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED),
            Context.RECEIVER_NOT_EXPORTED,
        )

        awaitClose { context.unregisterReceiver(receiver) }
    }

    private fun formatNext(): String? {
        val next = alarmManager?.nextAlarmClock ?: return null
        val trigger = next.triggerTime
        val now = System.currentTimeMillis()
        val delta = trigger - now
        val time = DateFormat.getTimeFormat(context).format(trigger)
        return if (delta > TimeUnit.HOURS.toMillis(24)) {
            val day = DateFormat.getDateFormat(context).format(trigger)
            "alarm $day $time"
        } else {
            "alarm $time"
        }
    }
}
