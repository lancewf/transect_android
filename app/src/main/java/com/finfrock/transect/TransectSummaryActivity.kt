package com.finfrock.transect

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.*
import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.Observation
import com.finfrock.transect.model.Sighting
import com.finfrock.transect.model.Transect
import com.finfrock.transect.model.WeatherObservation
import com.finfrock.transect.network.TransectApiService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.appbar.MaterialToolbar
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TransectSummaryActivity: AppCompatActivity(), OnMapReadyCallback  {
    companion object {
        const val TRANSECT_ID = "transectId"
    }

    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var transectApiService: TransectApiService
    lateinit var dataSource: DataSource
    private val timeFormat = DateTimeFormatter.ofPattern("hh:mm a")
    private lateinit var transectWithObservations: LiveData<Pair<Transect?, List<Observation>>>
    private lateinit var transectId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MyApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)

        dataSource = ViewModelProvider(viewModelStore, DataSource.FACTORY(database, transectApiService))
            .get(DataSource::class.java)
        setContentView(R.layout.transect_summary_activity)

        transectId = intent.extras?.getString(TRANSECT_ID) ?: ""
        if (transectId.isEmpty()) {
            Toast.makeText(this, "transectId not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        transectWithObservations = dataSource.getTransectWithObservations(transectId)

        val actionBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        actionBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)

        actionBar.setNavigationOnClickListener {
            finish()
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
//        dataSource.backgroundTransectWithObservations(transectId){
//           transect, observations ->
        transectWithObservations.observe(this, Observer<Pair<Transect?, List<Observation>>>{
                (transect, observations) ->

           if(transect != null) {
               map.clear()
               map.addMarker(createStartMarker(transect))
               map.addMarker(createEndMarker(transect))

               createObservationMarkers(observations).forEach { map.addMarker(it) }

               map.addPolyline(createPolyline(observations, transect))

               val bounds = findLatLngBounds(transect, observations)
               map.moveCamera(CameraUpdateFactory.newLatLngZoom(bounds.center, 11.0f))
           } else {
                Toast.makeText(this, "transect not found", Toast.LENGTH_SHORT).show()
                finish()
           }
        })
    }

    // drop the first observation because it overlaps the start location
    private fun createObservationMarkers(obs:List<Observation>):List<MarkerOptions> {
        return obs.drop(1).map{ createObservationMarker(it) }
    }

    private fun createPolyline(obs:List<Observation>, transect: Transect): PolylineOptions {
        // collection lat lng in order
        val locations = obs.map{it.location}.drop(1) + transect.endLatLon

        val (_, polyline) = locations.fold(Pair(transect.startLatLon, PolylineOptions()))
            { (previous, polyline), current ->
                polyline.add( previous, current )
                Pair(current, polyline)
            }

        return polyline
    }

    private fun createStartMarker(transect: Transect): MarkerOptions {
        return MarkerOptions()
            .position(transect.startLatLon)
            .title(transect.startDate.format(timeFormat))
            .icon(getMarkerIconFromDrawable(R.drawable.ic_baseline_home_24))
    }

    private fun createEndMarker(transect: Transect): MarkerOptions {
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

    private fun findLatLngBounds(transect: Transect, obs:List<Observation>): LatLngBounds {
            val locations = obs.map{it.location} + transect.startLatLon + transect.endLatLon

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