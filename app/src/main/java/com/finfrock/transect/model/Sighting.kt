package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class Sighting(
   override val id: String,
   override val datetime: LocalDateTime,
   override val location: LatLng,
   val count: Int,
   val distanceKm: Double,
   val bearing: Int,
   val groupType: GroupType
): Observation

data class SightingNullable(
   override val id: String,
   override val datetime: LocalDateTime? = null,
   override val location: LatLng? = null,
   val count: Int? = null,
   val distanceKm: Double? = null,
   val bearing: Int? = null,
   val groupType: GroupType? = null
): ObservationNullable

data class SightingMutable(
   override val id: String,
   override var datetime: LocalDateTime? = null,
   override var location: LatLng? = null,
   var count: Int? = null,
   var distanceKm: Double? = null,
   var bearing: Int? = null,
   var groupType: GroupType? = null
): ObservationMutable {

   override fun isValid(): Boolean {
      return datetime != null &&
         location != null &&
         count != null &&
         distanceKm != null &&
         bearing != null &&
         groupType != null
   }

   override fun toObservationNullable(): ObservationNullable {
      return SightingNullable(id, datetime, location, count, distanceKm, bearing, groupType )
   }

   override fun toObservation(): Observation? {
      return if (isValid()){
         Sighting(id,
            datetime!!, location!!, count!!, distanceKm!!, bearing!!, groupType!!
         )
      } else {
         null
      }
   }

   override fun clone(): ObservationMutable {
      return SightingMutable(id, datetime, location, count, distanceKm, bearing, groupType )
   }
}

enum class GroupType {
   MC, MCE, CG, UNKNOWN
}