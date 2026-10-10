package app.plain.launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_entry")
data class AppEntryEntity(
    @PrimaryKey val packageName: String,
    val tier: String,
    val position: Int,
    val rules: String? = null,
)
