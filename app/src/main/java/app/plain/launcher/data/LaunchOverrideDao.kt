package app.plain.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LaunchOverrideDao {
    @Query("SELECT * FROM launch_override")
    suspend fun getAll(): List<LaunchOverrideEntity>

    @Query("SELECT activityClassName FROM launch_override WHERE packageName = :packageName LIMIT 1")
    suspend fun getActivityClass(packageName: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: LaunchOverrideEntity)

    @Query("DELETE FROM launch_override WHERE packageName = :packageName")
    suspend fun delete(packageName: String)

    @Query("DELETE FROM launch_override")
    suspend fun deleteAll()
}
