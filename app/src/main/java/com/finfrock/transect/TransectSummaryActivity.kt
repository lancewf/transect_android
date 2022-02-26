package com.finfrock.transect

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.Observation
import com.finfrock.transect.model.Sighting
import com.finfrock.transect.model.Transect
import com.finfrock.transect.model.WeatherObservation
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

    private val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
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

    override fun onMapReady(map: GoogleMap) {
        map.addMarker(createStartMarker())
        map.addMarker(createEndMarker())

        createObservationMarkers().forEach{ map.addMarker(it) }

        map.addPolyline(createPolyline())

        val bounds = findLatLngBounds()
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 11.0f))
    }

    // drop the first observation because it overlaps the start location
    private fun createObservationMarkers():List<MarkerOptions> {
        return transect.obs.drop(1).map{ createObservationMarker(it) }
    }

    private fun createPolyline(): PolylineOptions {
        // collection lat lng in order
        val locations = transect.obs.map{it.location}.drop(1) + transect.endLatLon

        val (_, polyline) = locations.fold(Pair(transect.startLatLon, PolylineOptions()))
            { (previous, polyline), current ->
                polyline.add( previous, current )
                Pair(current, polyline)
            }

        return polyline
    }

    private fun createStartMarker(): MarkerOptions {
        return MarkerOptions()
            .position(transect.startLatLon)
            .title(transect.startDate.format(timeFormat))
            .icon(getMarkerIconFromDrawable(R.drawable.ic_baseline_home_24))
    }

    private fun createEndMarker(): MarkerOptions {
        return MarkerOptions()
            .position(transect.endLatLon)
            .title(transect.endDate.format(timeFormat))
            .icon(getMarkerIconFromDrawable(R.drawable.ic_baseline_flag_24))
    }

    private fun createObservationMarker(observation: Observation): MarkerOptions {
        return when(observation) {
            is Sighting ->
                MarkerOptions()
                    .position(observation.location)
                    .title(observation.datetime.format(timeFormat))
                    .icon(getMarkerIconFromDrawable(R.drawable.ic_bigfish2))
            is WeatherObservation ->
                MarkerOptions()
                    .position(observation.location)
                    .title(observation.datetime.format(timeFormat))
                    .icon(getMarkerIconFromDrawable(R.drawable.ic_icons8_sun))
            else ->
                MarkerOptions()
                    .position(observation.location)
                    .title(observation.datetime.format(timeFormat))
                    .icon(getMarkerIconFromDrawable(R.drawable.ic_baseline_question_mark_24))

        }
    }

    private fun findLatLngBounds(): LatLngBounds {
            val locations = transect.obs.map{it.location} + transect.startLatLon + transect.endLatLon

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