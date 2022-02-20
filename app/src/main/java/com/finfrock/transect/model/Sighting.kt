package com.finfrock.transect.model

import java.util.*
import com.google.android.gms.maps.model.LatLng

data class Sighting(
   val datetime: Date,
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