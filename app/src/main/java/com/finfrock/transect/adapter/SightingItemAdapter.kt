package com.finfrock.transect.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.R
import com.finfrock.transect.adapter.holder.ObservationItemViewHolder
import com.finfrock.transect.adapter.holder.SightingItemViewHolder
import com.finfrock.transect.adapter.holder.WeatherItemViewHolder
import com.finfrock.transect.model.*
import com.finfrock.transect.util.LocationProxyLike
import com.google.android.gms.maps.model.LatLng
import java.time.LocalDateTime

class SightingItemAdapter(private val observations: ObservationBuilder
): RecyclerView.Adapter<ObservationItemViewHolder>() {

    fun addNewWeatherObservation(): String {
        val newObsId = observations.createNewWeatherObservation()
        notifyItemInserted(itemCount -1 )
        return newObsId
    }

    fun addNewSighting(): String {
        val newObsId = observations.createNewSighting()
        notifyItemInserted(itemCount -1 )
        return newObsId
    }

    fun addAll(newObservations: List<Observation>) {
        if( newObservations.isNotEmpty()) {
            observations.setAllObs(newObservations)

            notifyDataSetChanged()
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (observations.isSightingAt(position)) {
            return R.layout.sighting_item
        }
        if (observations.isWeatherAt(position)) {
            return R.layout.weather_item
        }
        return 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ObservationItemViewHolder {
        val holder = if (viewType == R.layout.sighting_item) {
            createSightingViewHolder(parent)
        } else if (viewType == R.layout.weather_item ) {
            createWeatherViewHolder(parent)
        } else {
            throw Exception("View type $viewType not found")
        }

        return holder
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
        holder.display(observations.readOnlyAt(position))
    }

    override fun getItemCount() = observations.size()
}
