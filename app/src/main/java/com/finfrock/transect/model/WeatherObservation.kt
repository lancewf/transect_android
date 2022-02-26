package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

data class WeatherObservation(
    override val datetime: LocalDateTime,
    override val location: LatLng,
    var beaufort: Int? = null,
    var weather: Int? = null
): Observation
