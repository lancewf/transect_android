package com.finfrock.transect.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.R
import com.finfrock.transect.RunningTransectActivity
import com.finfrock.transect.VesselSummaryActivity
import com.finfrock.transect.model.VesselSummary

class VesselSummaryItemAdapter(private val context: Context, private val vesselSummaries: List<VesselSummary>):
    RecyclerView.Adapter<VesselSummaryItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.vessel_name)
        val numberTransectsTextView: TextView = view.findViewById(R.id.vessel_number_transects)
        val numberSightsTextView: TextView = view.findViewById(R.id.vessel_number_sightings)
        val animalPerKmTextView: TextView = view.findViewById(R.id.vessel_animals_per_km)
        val totalDurationTextView: TextView = view.findViewById(R.id.vessel_total_duration)
        val distanceTraveledTextView: TextView = view.findViewById(R.id.vessel_distance_traveled)
        val container: ConstraintLayout = view.findViewById(R.id.vessel_summary_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(R.layout.vessel_list_item, parent, false)

        val holder = ItemViewHolder(adapterLayout)
        holder.container.setOnClickListener {
            val summary = vesselSummaries[holder.adapterPosition]

            val intent = Intent(context, VesselSummaryActivity::class.java)

            intent.putExtra(VesselSummaryActivity.VESSEL_ID, summary.id)

            context.startActivity(intent)
        }

        return holder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val vessel = vesselSummaries[position]

        holder.nameTextView.text = vessel.name
        holder.numberTransectsTextView.text = "# Transects: ${vessel.numberOfTransects}"
        holder.numberSightsTextView.text = "# Sightings: ${vessel.numberOfSightings}"
        holder.animalPerKmTextView.text = "Animals/km: ${vessel.animalsPerKm}"
        holder.totalDurationTextView.text = "Total Duration: ${vessel.totalDuration}"
        holder.distanceTraveledTextView.text = "Distance Traveled (km): ${vessel.totalDistanceTraveledKm}"
    }

    override fun getItemCount() = vesselSummaries.size

}
