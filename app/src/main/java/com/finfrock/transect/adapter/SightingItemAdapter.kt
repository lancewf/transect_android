package com.finfrock.transect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.util.LocationProxyLike
import com.finfrock.transect.R
import com.finfrock.transect.adapter.holder.ObservationItemViewHolder
import com.finfrock.transect.adapter.holder.SightingItemViewHolder
import com.finfrock.transect.adapter.holder.WeatherItemViewHolder
import com.finfrock.transect.model.*
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

class SightingItemAdapter(private val observations: MutableList<Observation>,
                          private val locationProxy: LocationProxyLike
): RecyclerView.Adapter<ObservationItemViewHolder>() {

    fun addNewWeatherObservation() {
        val datetime = LocalDateTime.now()
        locationProxy.getLocation().addOnSuccessListener {
            addNewWeatherObservation(it, datetime)
        }
    }

    fun addNewWeatherObservation(latLng: LatLng, datetime: LocalDateTime) {
        observations.add(WeatherObservation(
            datetime = datetime,
            location = latLng
        ))
        notifyItemInserted(itemCount -1 )
    }

    fun addNewSighting() {
        locationProxy.getLocation().addOnSuccessListener {
            observations.add(Sighting(
                datetime = LocalDateTime.now(),
                location = it
            ))
            notifyItemInserted(itemCount -1 )
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = observations[position]
        if (item is Sighting) {
            return R.layout.sighting_item
        }
        if (item is WeatherObservation) {
            return R.layout.weather_item
        }
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationItemViewHolder {
        if (viewType == R.layout.sighting_item) {
            return createSightingViewHolder(parent)
        }
        if (viewType == R.layout.weather_item ) {
            return createWeatherViewHolder(parent)
        }

        throw Exception("View type $viewType not found")
    }

    private fun createWeatherViewHolder(parent: ViewGroup): WeatherItemViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(
                R.layout.weather_item, parent, false)

        return WeatherItemViewHolder(adapterLayout, observations)
    }

    private fun createSightingViewHolder(parent: ViewGroup): SightingItemViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(
                R.layout.sighting_item, parent, false)

        return SightingItemViewHolder(adapterLayout, observations)
    }

    override fun onBindViewHolder(holder: ObservationItemViewHolder, position: Int) {
        holder.display(observations[position])
    }

    override fun getItemCount() = observations.size

}