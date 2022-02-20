package com.finfrock.transect

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

class MockLocationProxy: LocationProxyLike {

    override fun getLocation(): Task<LatLng> {
        val taskCompletionSource = TaskCompletionSource<LatLng>()
        taskCompletionSource.setResult(LatLng(0.0, 0.0))

        return taskCompletionSource.task
    }
}