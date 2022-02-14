package com.finfrock.transect

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.finfrock.transect.model.LatLon
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

class LocationProxy(private val context: Context,
                    private val fusedLocationClient: FusedLocationProviderClient): LocationProxyLike {

    @SuppressLint("MissingPermission")
    override fun getLocation(): Task<LatLon> {
        val cts = CancellationTokenSource()
        val taskCompletionSource = TaskCompletionSource<LatLon>()
        if (hasPermissions()) {
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cts.token).addOnSuccessListener {
                taskCompletionSource.setResult(LatLon(it.latitude, it.longitude))
            }.addOnFailureListener{
                Toast.makeText(context, "Failure getting location", Toast.LENGTH_LONG)
                taskCompletionSource.setException(it)
            }
        } else {
            taskCompletionSource.setResult(LatLon(0.0, 0.0))
        }

        return taskCompletionSource.task
    }

    private fun hasPermissions():Boolean {
        return (
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                )
    }
}