package com.finfrock.transect.adapter.holder

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
    private val bearingTextView: TextView = view.findViewById(R.id.bearingLabel)
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
        bearingTextView.text = transect.bearing.toString()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy h:mm a")
        transect.startDate.format(formatter)
        dateTimeTextView.text = transect.startDate.format(formatter)

        val totalSeconds = transect.endDate.toEpochSecond(ZoneOffset.UTC) - transect.startDate.toEpochSecond(ZoneOffset.UTC)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        durationTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    fun setOnClickListener(listener: View.OnClickListener ) {
        container.setOnClickListener(listener)
    }
}