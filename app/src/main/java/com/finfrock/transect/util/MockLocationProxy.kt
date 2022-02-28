package com.finfrock.transect.util

import android.os.Handler
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

class MockLocationProxy: LocationProxyLike {

    override fun getLocation(): Task<LatLng> {
        val taskCompletionSource = TaskCompletionSource<LatLng>()
        Handler().postDelayed(RunALot(taskCompletionSource), 1000)

        return taskCompletionSource.task
    }
}

class RunALot(private val taskCompletionSource: TaskCompletionSource<LatLng>): Runnable {
    override fun run() {
        taskCompletionSource.setResult(LatLng(0.0, 0.0))
    }

}