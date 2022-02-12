package com.finfrock.transect.model

data class VesselSummary(
    val name: String,
    val id: Int,
    val numberOfTransects: Int,
    val numberOfSightings: Int,
    val animalsPerKm: Double,
    val totalDistanceTraveledKm: Double,
    val totalDuration: String
) {
    override fun toString(): String = name
}