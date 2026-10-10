package app.plain.launcher.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.firstRunDataStore by preferencesDataStore(name = "plain_first_run")

class FirstRunStore(private val context: Context) : FirstRunPreferences {
    private val keyDone = booleanPreferencesKey("firstRunDone")

    override val firstRunDone: Flow<Boolean> =
        context.firstRunDataStore.data.map { prefs -> prefs[keyDone] ?: false }

    override suspend fun setFirstRunDone() {
        context.firstRunDataStore.edit { prefs -> prefs[keyDone] = true }
    }

    suspend fun isFirstRunDone(): Boolean = firstRunDone.first()
}
