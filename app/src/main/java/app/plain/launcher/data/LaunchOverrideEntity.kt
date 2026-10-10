package app.plain.launcher.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launch_override")
data class LaunchOverrideEntity(
    @PrimaryKey val packageName: String,
    val activityClassName: String,
)
