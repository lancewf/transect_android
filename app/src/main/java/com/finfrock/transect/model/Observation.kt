package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

interface Observation {
    val datetime: LocalDateTime
    val location: LatLng
}

interface ObservationNullable {
    val datetime: LocalDateTime?
    val location: LatLng?
}

interface ObservationMutable {
    var datetime: LocalDateTime?
    var location: LatLng?

    fun toObservation(): Observation?
    fun toObservationNullable(): ObservationNullable
    fun isValid(): Boolean
    fun clone(): ObservationMutable
}
