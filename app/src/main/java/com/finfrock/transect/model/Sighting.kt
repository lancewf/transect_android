package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class Sighting(
   override val datetime: LocalDateTime,
   override val location: LatLng,
   var count: Int? = null,
   var distanceKm: Double? = null,
   var bearing: Int? = null,
   var groupType: GroupType? = null
): Observation

data class WeatherObservation(
   override val datetime: LocalDateTime,
   override val location: LatLng,
   var beaufort: Int? = null,
   var weather: Int? = null
): Observation

interface Observation {
    val datetime: LocalDateTime
    val location: LatLng
}

enum class GroupType {
   MC, MCE, CG, UNKNOWN
}