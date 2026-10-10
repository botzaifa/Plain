package app.plain.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppEntryDao {
    @Query("SELECT * FROM app_entry")
    fun observeAll(): Flow<List<AppEntryEntity>>

    @Query("SELECT * FROM app_entry")
    suspend fun getAll(): List<AppEntryEntity>

    @Query("SELECT * FROM app_entry WHERE packageName = :packageName LIMIT 1")
    suspend fun get(packageName: String): AppEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: AppEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<AppEntryEntity>)

    @Query("DELETE FROM app_entry WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("DELETE FROM app_entry")
    suspend fun deleteAll()
}
