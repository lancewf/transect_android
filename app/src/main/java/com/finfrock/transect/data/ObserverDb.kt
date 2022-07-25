package com.finfrock.transect.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observer")
data class ObserverDb(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
)
