package com.finfrock.transect.network

import com.squareup.moshi.Json
import java.time.Duration

data class RemoteObservation(
    val id:String,
    @Json(name="transect_id") val transectId:String,
    @Json(name="obs_type") val obType:String,
    val date: Int,
    val lat: Double,
    val lon: Double,
    val bearing: Int?,
    val count: Int?,
    @Json(name="distance_km") val distanceKm: Double?,
    @Json(name="group_type") val groupType: String?,
    @Json(name="beaufort_type") val beaufortType: String?,
    @Json(name="weather_type") val weatherType: String?,
)

data class RemoteTransect(
    val id:String,
    @Json(name="start_date") val startDate: Int,
    @Json(name="start_lat") val startLat: Double,
    @Json(name="start_lon") val startLon: Double,
    @Json(name="end_date") val endDate: Int,
    @Json(name="end_lat") val endLat: Double,
    @Json(name="end_lon") val endLon: Double,
    @Json(name="vessel_id") val vesselId: String,
    val bearing: Int,
    @Json(name="observer1_id") val observer1Id: String,
    @Json(name="observer2_id") val observer2Id: String?,
    val observations: List<RemoteObservation>
)

data class RemoteObserver(
    val id:String,
    val name: String
)

data class RemoteVessel(
    val id:String,
    val name: String,
    @Json(name="number_of_transects") val numberOfTransects: Int,
    @Json(name="number_of_sightings") val numberOfSightings: Int,
    @Json(name="total_duration_of_all_transects_secs") val totalDurationOfAllTransectsSec: Int,
    @Json(name="total_distance_of_all_transects_km") val totalDistanceOfAllTransectsKm: Double,
    @Json(name="number_of_animals") val numberOfAnimals: Int,
)
