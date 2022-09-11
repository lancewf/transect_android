package com.finfrock.transect.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vessel")
data class VesselDb(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "number_of_transects") val numberOfTransects: Int,
    @ColumnInfo(name = "number_of_sightings") val numberOfSightings: Int,
    @ColumnInfo(name = "number_of_animals") val numberOfAnimals: Int,
    @ColumnInfo(name = "total_duration_of_all_transects_secs") val totalDurationOfAllTransectsSec: Int,
    @ColumnInfo(name = "total_distance_of_all_transects_km") val totalDistanceOfAllTransectsKm: Double,
)
