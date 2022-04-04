package com.finfrock.transect.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transect")
data class TransectDb(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "start_date") val startDate: Int,
    @ColumnInfo(name = "end_date") val endDate: Int,
    @ColumnInfo(name = "start_lat") val startLat: Double,
    @ColumnInfo(name = "start_lon") val startLon: Double,
    @ColumnInfo(name = "end_lat") val endLat: Double,
    @ColumnInfo(name = "end_lon") val endLon: Double,
    @ColumnInfo(name = "vessel_id") val vesselId: Int,
    @ColumnInfo(name = "bearing") val bearing: Int,
    @ColumnInfo(name = "observer1_id") val observer1Id: Int,
    @ColumnInfo(name = "observer2_id") val observer2Id: Int? = null
)
