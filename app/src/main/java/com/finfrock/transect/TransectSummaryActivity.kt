package com.finfrock.transect

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.Transect
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.appbar.MaterialToolbar
import java.time.format.DateTimeFormatter


class TransectSummaryActivity: AppCompatActivity(), OnMapReadyCallback  {
    companion object {
        const val TRANSECT_ID = "transectId"
    }

    private lateinit var transect: Transect

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transect_summary_activity)

        val transectId = intent.extras?.getString(TRANSECT_ID) ?: ""
        if (transectId.isEmpty()) {
            Toast.makeText(this, "transectId not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        val foundTransect = DataSource.getTransectFromId(transectId)

        if (foundTransect == null) {
            Toast.makeText(this, "transect not found id: " + transectId, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        this.transect = foundTransect

        val actionBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        actionBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)

        actionBar.setNavigationOnClickListener {
            finish()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
        googleMap.addMarker(
            MarkerOptions()
                .position(transect.startLatLon)
                .title(transect.startDate.format(timeFormat))
                .icon(getMarkerIconFromDrawable(R.drawable.ic_baseline_home_24))
        )
        googleMap.addMarker(
            MarkerOptions()
                .position(transect.endLatLon)
                .title(transect.endDate.format(timeFormat))
                .icon(getMarkerIconFromDrawable(R.drawable.ic_baseline_flag_24))
        )
        var previousLatLng = transect.startLatLon
        transect.obs.drop(1).forEach{
            if(it.isSighting()) {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(it.location())
                        .title(it.datetime().format(timeFormat))
                        .icon(getMarkerIconFromDrawable(R.drawable.ic_bigfish2))
                )
            } else {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(it.location())
                        .title(it.datetime().format(timeFormat))
                        .icon(getMarkerIconFromDrawable(R.drawable.ic_icons8_sun))
                )
            }
            googleMap.addPolyline(PolylineOptions().add(
                previousLatLng, it.location()
            ))

            previousLatLng = it.location()
        }

        googleMap.addPolyline(PolylineOptions().add(
            previousLatLng, transect.endLatLon
        ))

        val bounds = findLatLngBounds()
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 11.0f))
    }

    private fun findLatLngBounds(): LatLngBounds {
        val locations = transect.obs.map{it.location()} + transect.startLatLon + transect.endLatLon

        val latMax = locations.maxOfOrNull{it.latitude}
        val latMin = locations.minOfOrNull{it.latitude}
        val longMax = locations.maxOfOrNull{it.longitude}
        val longMin = locations.minOfOrNull{it.longitude}

        return if (latMax == null || latMin == null || longMax == null || longMin == null) {
            LatLngBounds(
                LatLng(20.680398, -156.652441),
                LatLng(20.780584, -156.504399)
            )
        } else {
            LatLngBounds(
                LatLng(latMin, longMin),
                LatLng(latMax, longMax)
            )
        }
    }

    private fun getMarkerIconFromDrawable(@DrawableRes id: Int): BitmapDescriptor? {
        val drawable = resources.getDrawable(id)
        val canvas = Canvas()
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}