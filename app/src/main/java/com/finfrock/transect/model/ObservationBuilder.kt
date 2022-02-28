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

    fun removeAt(index: Int) {
        sightings.removeAt(index)
        listeners.forEach{it()}
    }
}