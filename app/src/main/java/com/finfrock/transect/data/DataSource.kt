package com.finfrock.transect.data

import com.finfrock.transect.model.VesselSummary

class DataSource {

    fun loadVesselSummaries(): List<VesselSummary> {
        return listOf(
            VesselSummary(name = "Kohola", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(name = "Ohua", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57")
        )
    }
}