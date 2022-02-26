package com.finfrock.transect.util

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task

interface LocationProxyLike {
    fun getLocation(): Task<LatLng>
}