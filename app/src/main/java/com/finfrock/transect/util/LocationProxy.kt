package com.finfrock.transect.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource

class LocationProxy(private val context: Context,
                    private val fusedLocationClient: FusedLocationProviderClient):
    LocationProxyLike {

    @SuppressLint("MissingPermission")
    override fun getLocation(): Task<LatLng> {
        val cts = CancellationTokenSource()
        val taskCompletionSource = TaskCompletionSource<LatLng>()
        if (hasPermissions()) {
            fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, cts.token).addOnSuccessListener {
                taskCompletionSource.setResult(LatLng(it.latitude, it.longitude))
            }.addOnFailureListener{
                Toast.makeText(context, "Failure getting location", Toast.LENGTH_LONG)
                taskCompletionSource.setException(it)
            }
        } else {
            taskCompletionSource.setResult(LatLng(0.0, 0.0))
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