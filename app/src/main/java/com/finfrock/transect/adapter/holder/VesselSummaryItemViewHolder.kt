package com.finfrock.transect.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.R
import com.finfrock.transect.model.VesselSummary
import kotlin.math.roundToInt

class VesselSummaryItemViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
    private val nameTextView: TextView = view.findViewById(R.id.vessel_name)
    private val numberTransectsTextView: TextView = view.findViewById(R.id.vessel_number_transects)
    private val numberSightsTextView: TextView = view.findViewById(R.id.vessel_number_sightings)
    private val animalPerKmTextView: TextView = view.findViewById(R.id.vessel_animals_per_km)
    private val totalDurationTextView: TextView = view.findViewById(R.id.vessel_total_duration)
    private val distanceTraveledTextView: TextView = view.findViewById(R.id.vessel_distance_traveled)
    private val container: ConstraintLayout = view.findViewById(R.id.vessel_summary_container)

    fun setOnClickListener(listener: View.OnClickListener ) {
        container.setOnClickListener(listener)
    }

    fun display(vessel: VesselSummary) {
        val totalDurationInHours = vessel.totalDuration / 3600.0
        nameTextView.text = vessel.name
        numberTransectsTextView.text = "# Transects: ${vessel.numberOfTransects}"
        numberSightsTextView.text = "# Sightings: ${vessel.numberOfSightings}"
        animalPerKmTextView.text = "Animals/km: ${"%,.2f".format(vessel.animalsPerKm)}"
        totalDurationTextView.text = "${"%,.1f".format(totalDurationInHours)} hrs"
        distanceTraveledTextView.text = "${"%,.0f".format(vessel.totalDistanceTraveledKm)} km"
    }
}