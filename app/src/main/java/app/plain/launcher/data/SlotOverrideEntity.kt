package app.plain.launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "slot_override")
data class SlotOverrideEntity(
    @PrimaryKey val slot: String,
    val packageName: String,
)
