package com.finfrock.transect.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "observation")
data class ObservationDb(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "transect_id") val transectId: String,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "date") val datetime: Int,
    @ColumnInfo(name = "lat") val lat: Double,
    @ColumnInfo(name = "lon") val lon: Double,
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "distance_km") val distanceKm: Double,
    @ColumnInfo(name = "bearing") val bearing: Int,
    @ColumnInfo(name = "group_type") val groupType: Int,
    @ColumnInfo(name = "beaufort") val beaufort: Int,
    @ColumnInfo(name = "weather") val weather: Int
)
