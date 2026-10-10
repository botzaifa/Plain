package app.plain.launcher.data

import kotlinx.coroutines.flow.Flow

interface FirstRunPreferences {
    val firstRunDone: Flow<Boolean>
    suspend fun setFirstRunDone()
}
