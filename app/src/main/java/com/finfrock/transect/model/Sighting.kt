package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class Sighting(
   val datetime: LocalDateTime,
   val location: LatLng,
   var count: Int? = null,
   var distanceKm: Double? = null,
   var bearing: Int? = null,
   var groupType: GroupType? = null,
   var beaufort: Int? = null,
   var weather: Int? = null
)

enum class GroupType {
   MC, MCE, CG, UNKNOWN
}