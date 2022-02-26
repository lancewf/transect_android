package com.finfrock.transect.model

import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

interface Observation {
    val datetime: LocalDateTime
    val location: LatLng
}
