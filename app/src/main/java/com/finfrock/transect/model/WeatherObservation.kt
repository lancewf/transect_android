package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class WeatherObservation(
    override val id: String,
    override val datetime: LocalDateTime,
    override val location: LatLng,
    val beaufort: Int,
    val weather: Int
): Observation

data class WeatherObservationNullable(
    override val id: String,
    override val datetime: LocalDateTime? = null,
    override val location: LatLng? = null,
    val beaufort: Int? = null,
    val weather: Int? = null
): ObservationNullable

data class WeatherObservationMutable(
    override val id: String,
    override var datetime: LocalDateTime? = null,
    override var location: LatLng? = null,
    var beaufort: Int? = null,
    var weather: Int? = null
): ObservationMutable {

    override fun isValid(): Boolean {
        return datetime != null &&
            location != null &&
            beaufort != null &&
            weather != null
    }

    override fun toObservation(): Observation? {
        return if (isValid()){
            WeatherObservation(
                id, datetime!!, location!!, beaufort!!, weather!!
            )
        } else {
            null
        }
    }

    override fun toObservationNullable(): ObservationNullable {
        return WeatherObservationNullable(id, datetime, location, beaufort, weather)
    }

    override fun clone(): ObservationMutable {
        return WeatherObservationMutable(id, datetime, location, beaufort, weather)
    }
}

