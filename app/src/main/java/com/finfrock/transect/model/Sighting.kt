package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class Sighting(
   val datetime: LocalDateTime,
   val location: LatLng,
   var count: Int? = null,
   var distanceKm: Double? = null,
   var bearing: Int? = null,
   var groupType: GroupType? = null
)

data class WeatherObservation(
   val datetime: LocalDateTime,
   val location: LatLng,
   var beaufort: Int? = null,
   var weather: Int? = null
)

data class TransectItem(
   var sighting: Sighting? = null,
   var weatherObs: WeatherObservation? = null
) {
 fun location(): LatLng {
   if (sighting != null) {
      return sighting!!.location
   }
   if (weatherObs != null) {
       return weatherObs!!.location
   }
     throw Exception("either sighting or weather obs was set")
 }
    fun isSighting(): Boolean {
        return sighting != null
    }
    fun datetime(): LocalDateTime {
        if (sighting != null) {
            return sighting!!.datetime
        }
        if (weatherObs != null) {
            return weatherObs!!.datetime
        }
        throw Exception("either sighting or weather obs was set")
    }
    override fun toString(): String {
        if (sighting != null) {
            return "sighting"
        }
        if (weatherObs != null) {
            return "weatherObs"
        }

        return "none"
    }
}

enum class GroupType {
   MC, MCE, CG, UNKNOWN
}