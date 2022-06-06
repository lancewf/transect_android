package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class ActiveTransect(
    val id: String,
    val startDate: LocalDateTime,
    val startLatLon: LatLng,
    val vesselId: Int,
    val bearing: Int,
    val observer1Id: Int,
    val observer2Id: Int? = null
)