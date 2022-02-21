package com.finfrock.transect.model

import java.util.*
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class Transect(
    val id: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val startLatLon: LatLng,
    val endLatLon: LatLng,
    val obs: List<TransectItem>,
    val vesselId: Int,
    val bearing: Int,
    val observer1Id: Int,
    val observer2Id: Int? = null
)
