package com.finfrock.transect

import com.finfrock.transect.model.LatLon
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

class MockLocationProxy: LocationProxyLike {

    override fun getLocation(): Task<LatLon> {
        val taskCompletionSource = TaskCompletionSource<LatLon>()
        taskCompletionSource.setResult(LatLon(0.0, 0.0))

        return taskCompletionSource.task
    }
}