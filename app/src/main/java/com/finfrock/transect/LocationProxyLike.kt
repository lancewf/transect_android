package com.finfrock.transect

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task

interface LocationProxyLike {
    fun getLocation(): Task<LatLng>
}