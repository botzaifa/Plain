package app.plain.launcher.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GatePassDao {
    @Query("SELECT * FROM gate_pass_day WHERE date = :date LIMIT 1")
    suspend fun get(date: String): GatePassEntity?

    @Query("SELECT * FROM gate_pass_day ORDER BY date DESC LIMIT :days")
    suspend fun recent(days: Int): List<GatePassEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GatePassEntity)

    @Query("DELETE FROM gate_pass_day WHERE date < :beforeDate")
    suspend fun deleteOlderThan(beforeDate: String)
}
