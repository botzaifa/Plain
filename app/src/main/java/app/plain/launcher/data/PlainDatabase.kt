package app.plain.launcher.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.withTransaction

interface RestoreDefaultsGateway {
    suspend fun restoreDefaultsInTransaction(planned: List<AppEntryEntity>)
}

@Database(
    entities = [
        AppEntryEntity::class,
        SlotOverrideEntity::class,
        LaunchOverrideEntity::class,
        GatePassEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class PlainDatabase : RoomDatabase(), RestoreDefaultsGateway {
    abstract fun appEntryDao(): AppEntryDao
    abstract fun slotOverrideDao(): SlotOverrideDao
    abstract fun launchOverrideDao(): LaunchOverrideDao
    abstract fun gatePassDao(): GatePassDao

    override suspend fun restoreDefaultsInTransaction(planned: List<AppEntryEntity>) {
        withTransaction {
            appEntryDao().deleteAll()
            slotOverrideDao().deleteAll()
            launchOverrideDao().deleteAll()
            appEntryDao().upsertAll(planned)
        }
    }
}
