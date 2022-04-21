package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime
import java.util.*

class ObservationBuilder {
    private val sightings = mutableListOf<ObservationMutable>()
    private val listeners = mutableListOf<() -> Unit>()

    // Readonly

    fun size(): Int {
        return sightings.size
    }

    fun isEmpty(): Boolean {
        return sightings.isEmpty()
    }

    fun nonEmpty(): Boolean {
        return !isEmpty()
    }

    fun toList(): List<Observation> {
        return sightings.filter{it.isValid()}.map{it.toObservation()!!}
    }

    fun isSightingAt(index: Int): Boolean {
        return sightings[index] is SightingMutable
    }

    fun isWeatherAt(index: Int): Boolean {
        return sightings[index] is WeatherObservationMutable
    }

    fun readOnlyAt(index: Int): ObservationNullable {
        return sightings[index].toObservationNullable()
    }

    fun isValid():Boolean {
        return sightings.all{it.isValid()}
    }

    fun afterDataChanged(listener: () -> Unit) {
        listeners.add(listener)
    }

    // Writeable

    fun setAllObs(obs: List<Observation>) {
        obs.forEach{ob ->
            if ( ob is Sighting) {
                sightings.add(SightingMutable(
                    id = ob.id,
                    datetime = ob.datetime,
                    location = ob.location,
                    count = ob.count,
                    distanceKm = ob.distanceKm,
                    bearing = ob.bearing,
                    groupType = ob.groupType,
                ))
            } else if ( ob is WeatherObservation) {
                sightings.add(WeatherObservationMutable(
                    id = ob.id,
                    datetime = ob.datetime,
                    location = ob.location,
                    beaufort = ob.beaufort,
                    weather = ob.weather,
                ))
            }
        }
    }

    fun updateFromIndex(index: Int, update: (ObservationMutable) -> ObservationMutable) {
        val updateOb = update(sightings[index].clone())
        sightings[index] = updateOb
        listeners.forEach{it()}
    }

    fun updateFromId(id: String, update: (ObservationMutable) -> ObservationMutable) {
        val foundObs = sightings.find{it.id == id}
        if (foundObs != null) {
            val updateOb = update(foundObs.clone())
            val index = sightings.indexOf(foundObs)
            sightings[index] = updateOb
            listeners.forEach{it()}
        }
    }

    fun createNewWeatherObservation(): String {
        val id = UUID.randomUUID().toString()
        sightings.add(WeatherObservationMutable(
            id,
            datetime = LocalDateTime.now(),
        ))
        listeners.forEach{it()}

        return id
    }

    fun createNewSighting(): String {
        val id = UUID.randomUUID().toString()
        sightings.add(SightingMutable(
            id,
            datetime = LocalDateTime.now(),
            groupType = GroupType.UNKNOWN
        ))
        listeners.forEach{it()}

        return id
    }

    fun removeAt(index: Int): ObservationMutable {
        val ob = sightings.removeAt(index)
        listeners.forEach{it()}
        return ob
    }
}