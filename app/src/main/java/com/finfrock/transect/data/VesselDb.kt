package com.finfrock.transect.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vessel")
data class VesselDb(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
)
