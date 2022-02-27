package com.finfrock.transect.data

import com.finfrock.transect.model.*
import com.finfrock.transect.model.Observer
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.util.*

object DataSource {
    private val transects = mutableListOf<TransectState>()

    init {
        loadFakeData()
    }

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
        return transects.map{it.transect}.filter{it.vesselId == vesselId}
    }

    fun getTransectFromId(transectId: String): Transect?  {
        return transects.map{it.transect}.find{it.id == transectId}
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

    private fun loadFakeData() {
        val now = LocalDateTime.now()
        transects.add(
            TransectState(Transect(
                id = UUID.randomUUID().toString(),
                startDate = now,
                endDate = now.plusHours(2),
                startLatLon = LatLng(20.780584, -156.504399),
                endLatLon = LatLng(20.572826, -156.652441),
                obs = listOf(
                    WeatherObservation(
                        datetime = now,
                        location = LatLng(20.780584, -156.504399),
                        beaufort = 1,
                        weather = 1
                    ),
                    Sighting(
                        datetime = now.plusMinutes(33),
                        location = LatLng(20.730948, -156.529627),
                        count = 1,
                        distanceKm = 0.5,
                        bearing = 90,
                        groupType = GroupType.UNKNOWN
                    ),
                    WeatherObservation(
                        datetime = now.plusMinutes(43),
                        location = LatLng(20.711587, -156.536530),
                        beaufort = 1,
                        weather = 1
                    ),
                    Sighting(
                        datetime = now.plusMinutes(50),
                        location = LatLng(20.680398, -156.581261),
                        count = 2,
                        distanceKm = 0.5,
                        bearing = 90,
                        groupType = GroupType.UNKNOWN
                    ),
                    WeatherObservation(
                        datetime = now.plusMinutes(65),
                        location = LatLng(20.652363, -156.568204),
                        beaufort = 1,
                        weather = 1
                    ),
                    Sighting(
                        datetime = now.plusMinutes(83),
                        location = LatLng(20.630308, -156.609860),
                        count = 3,
                        distanceKm = 0.5,
                        bearing = 90,
                        groupType = GroupType.UNKNOWN
                    ),
                ),
                vesselId = 1, bearing = 90,
                observer1Id = 4, observer2Id = 5
            ), true )
        )

        transects.add(
            TransectState(Transect(id = UUID.randomUUID().toString(),
                startDate = LocalDateTime.now(), endDate = LocalDateTime.now(),
                startLatLon = LatLng(0.0, 0.0), endLatLon = LatLng(0.0, 0.0),
                obs = emptyList(),
                vesselId = 1, bearing = 77,
                observer1Id = 1, observer2Id = 2
            ), true )
        )
    }
}