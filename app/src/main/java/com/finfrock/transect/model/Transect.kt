package com.finfrock.transect.model

import java.util.*
import com.google.android.gms.maps.model.LatLng

data class Transect(
    val id: String,
    val startDate: Date,
    val endDate: Date,
    val startLatLon: LatLng,
    val endLatLon: LatLng,
    val sightings: List<Sighting>,
    val vesselId: Int,
    val bearing: Int,
    val observer1Id: Int,
    val observer2Id: Int? = null
)
