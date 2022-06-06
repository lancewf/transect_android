package com.finfrock.transect.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_transect")
data class ActiveTransectDb(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "start_date") val startDate: Int,
    @ColumnInfo(name = "start_lat") val startLat: Double,
    @ColumnInfo(name = "start_lon") val startLon: Double,
    @ColumnInfo(name = "vessel_id") val vesselId: String,
    @ColumnInfo(name = "bearing") val bearing: Int,
    @ColumnInfo(name = "observer1_id") val observer1Id: String,
    @ColumnInfo(name = "observer2_id") val observer2Id: String? = null
)
