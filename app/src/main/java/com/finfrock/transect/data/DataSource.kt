package com.finfrock.transect.data

import com.finfrock.transect.model.*
import com.finfrock.transect.model.Observer
import java.util.*

object DataSource {
    private val transects = mutableListOf<TransectState>()

    fun loadVesselSummaries(): List<VesselSummary> {
        return listOf(
            VesselSummary(id = 1, name = "Kohola", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(id = 2, name = "Ohua", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57"),
            VesselSummary(id = 3, name = "Aloha Kai", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(id = 4, name = "CaneFire II", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57"),
            VesselSummary(id = 5, name = "Kai Kanani", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(id = 6, name = "Trilogy V", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57")
        )
    }

    fun getTransectsWithVesselId(vesselId: Int): List<Transect> {
        return listOf(
            Transect(id = UUID.randomUUID(), startDate = Date(), endDate = Date(),
                startLatLon = LatLon(0.0, 0.0), endLatLon = LatLon(0.0, 0.0),
                sightings = emptyList(), vesselId = vesselId, bearing = 90,
                observer1Id = 4, observer2Id = 5
            ),
            Transect(id = UUID.randomUUID(), startDate = Date(), endDate = Date(),
                startLatLon = LatLon(0.0, 0.0), endLatLon = LatLon(0.0, 0.0),
                sightings = emptyList(), vesselId = vesselId, bearing = 77,
                observer1Id = 1, observer2Id = 2
            )
        )
//        transects.map{it.transect}.filter{it.vesselId == vesselId}
    }

    fun getAllTransects(): List<Transect> {
        return transects.map{ it.transect }
    }

    fun addTransect(transect: Transect){
        transects.add(TransectState(transect, true))
    }

    fun loadObservers(): List<Observer> {
        return listOf(
            Observer(id = 1, name = "Ed Lyman"),
            Observer(id = 2, name = "Grant Thompson"),
            Observer(id = 3, name = "Jason Moore"),
            Observer(id = 4, name = "Lance"),
            Observer(id = 5, name = "Rachel Finn"),
        )
    }
}