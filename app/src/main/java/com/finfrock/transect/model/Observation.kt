package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

interface Observation {
    val id: String
    val datetime: LocalDateTime
    val location: LatLng
}

interface ObservationNullable {
    val id: String
    val datetime: LocalDateTime?
    val location: LatLng?
}

interface ObservationMutable {
    val id: String
    var datetime: LocalDateTime?
    var location: LatLng?

    fun toObservation(): Observation?
    fun toObservationNullable(): ObservationNullable
    fun isValid(): Boolean
    fun clone(): ObservationMutable
}
