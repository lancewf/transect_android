package com.finfrock.transect.adapter.holder

import android.location.Location
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.R
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.Transect
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class TransectItemViewHolder(val view: View, private val lifecycleOwner: LifecycleOwner, val dataSource: DataSource): RecyclerView.ViewHolder(view) {
    private val sightingCountTextView: TextView = view.findViewById(R.id.sightingCount)
    private val observer1NameTextView: TextView = view.findViewById(R.id.observerName1)
    private val observer2NameTextView: TextView = view.findViewById(R.id.observerName2)
    private val distanceTextView: TextView = view.findViewById(R.id.distanceLabel)
    private val dateTimeTextView: TextView = view.findViewById(R.id.dateTime)
    private val durationTextView: TextView = view.findViewById(R.id.durationLabel)
    private val container: ConstraintLayout = view.findViewById(R.id.transect_list_container)

    fun display(transect: Transect) {
        dataSource.getObserverName(transect.observer1Id).observe(lifecycleOwner) {
                observer1NameTextView.text = it
            }

        dataSource.getObserverName(transect.observer2Id).observe(lifecycleOwner) {
                observer2NameTextView.text = it
            }

        dataSource.getAnimalCountForTransect(transect.id).observe(lifecycleOwner) { animalCount ->
            sightingCountTextView.text = animalCount.toString()
        }
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yy h:mm a")
        transect.startDate.format(formatter)
        dateTimeTextView.text = transect.startDate.format(formatter)

        val totalSeconds = transect.endDate.toEpochSecond(ZoneOffset.UTC) - transect.startDate.toEpochSecond(ZoneOffset.UTC)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        durationTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val distanceKm = calculateDistanceInKm(transect)

        distanceTextView.text = "${"%,.1f".format(distanceKm)} km"
    }

    private fun calculateDistanceInKm(transect: Transect): Float {
        val results =  FloatArray(3)
        Location.distanceBetween(transect.startLatLon.latitude,
            transect.startLatLon.longitude, transect.endLatLon.latitude,
            transect.endLatLon.longitude, results)

        return results[0] / 1000.0f
    }

    fun setOnClickListener(listener: View.OnClickListener ) {
        container.setOnClickListener(listener)
    }
}