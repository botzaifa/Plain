package app.plain.launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gate_pass_day")
data class GatePassEntity(
    @PrimaryKey val date: String,
    val count: Int,
)
