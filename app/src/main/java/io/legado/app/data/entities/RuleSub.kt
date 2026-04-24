package io.legado.app.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "ruleSubs")
data class RuleSub(
    @PrimaryKey
    val id: Long = System.currentTimeMillis(),
    var name: String = "",
    var url: String = "",
    var type: Int = 0,
    var customOrder: Int = 0,
    var autoUpdate: Boolean = false,
    var update: Long = System.currentTimeMillis(),
    @ColumnInfo(defaultValue = "0")
    var silentUpdate: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    var updateInterval: Int = 0
)
