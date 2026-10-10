package app.plain.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SlotOverrideDao {
    @Query("SELECT * FROM slot_override")
    fun observeAll(): Flow<List<SlotOverrideEntity>>

    @Query("SELECT * FROM slot_override")
    suspend fun getAll(): List<SlotOverrideEntity>

    @Query("SELECT packageName FROM slot_override WHERE slot = :slot LIMIT 1")
    suspend fun getPackage(slot: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SlotOverrideEntity)

    @Query("DELETE FROM slot_override WHERE slot = :slot")
    suspend fun delete(slot: String)

    @Query("DELETE FROM slot_override")
    suspend fun deleteAll()
}
