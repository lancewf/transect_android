package com.finfrock.transect.model

data class VesselSummary(
    val name: String,
    val id: String,
    val numberOfTransects: Int,
    val numberOfSightings: Int,
    val animalsPerKm: Double,
    val totalDistanceTraveledKm: Double,
    val totalDuration: Int
) {
    override fun toString(): String = name
}