package com.finfrock.transect.model

import java.util.*

data class Transect(
    val id: UUID,
    val startDate: Date,
    val endDate: Date,
    val startLatLon: LatLon,
    val endLatLon: LatLon,
    val sightings: List<Sighting>,
    val vesselId: Int,
    val bearing: Int,
    val observer1Id: Int,
    val observer2Id: Int? = null
)
