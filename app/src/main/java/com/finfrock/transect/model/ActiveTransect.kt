package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class ActiveTransect(
    val id: String,
    val startDate: LocalDateTime,
    val startLatLon: LatLng,
    val vesselId: String,
    val bearing: Int,
    val observer1Id: String,
    val observer2Id: String? = null
)
