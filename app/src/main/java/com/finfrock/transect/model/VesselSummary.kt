package com.finfrock.transect.model

data class VesselSummary(
    val name: String,
    val numberOfTransects: Int,
    val numberOfSightings: Int,
    val animalsPerKm: Double,
    val totalDistanceTraveledKm: Double,
    val totalDuration: String
)
