package com.finfrock.transect

import com.finfrock.transect.model.LatLon
import com.google.android.gms.tasks.Task

interface LocationProxyLike {
    fun getLocation(): Task<LatLon>
}