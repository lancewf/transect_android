package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

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

    fun update(index: Int, update: (ObservationMutable) -> ObservationMutable) {
        val updateOb = update(sightings[index].clone())
        sightings[index] = updateOb
        listeners.forEach{it()}
    }

    fun createNewWeatherObservation(datetime: LocalDateTime, latLng: LatLng) {
        sightings.add(WeatherObservationMutable(
            datetime = datetime,
            location = latLng,
        ))
        listeners.forEach{it()}
    }

    fun createNewSighting(datetime: LocalDateTime, latLng: LatLng) {
        sightings.add(SightingMutable(
            datetime = datetime,
            location = latLng,
            groupType = GroupType.UNKNOWN
        ))
        listeners.forEach{it()}
    }

    fun removeAt(index: Int) {
        sightings.removeAt(index)
        listeners.forEach{it()}
    }
}