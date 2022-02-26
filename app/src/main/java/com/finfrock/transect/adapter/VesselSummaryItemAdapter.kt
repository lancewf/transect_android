package com.finfrock.transect.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.R
import com.finfrock.transect.VesselSummaryActivity
import com.finfrock.transect.adapter.holder.VesselSummaryItemViewHolder
import com.finfrock.transect.model.VesselSummary

class VesselSummaryItemAdapter(private val context: Context,
                               private val vesselSummaries: List<VesselSummary>):
    RecyclerView.Adapter<VesselSummaryItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VesselSummaryItemViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(
                R.layout.vessel_list_item, parent, false)

        val holder = VesselSummaryItemViewHolder(adapterLayout)
        holder.setOnClickListener {
            val summary = vesselSummaries[holder.adapterPosition]

            val intent = Intent(context, VesselSummaryActivity::class.java)

            intent.putExtra(VesselSummaryActivity.VESSEL_ID, summary.id)

            context.startActivity(intent)
        }

        return holder
    }

    override fun onBindViewHolder(holder: VesselSummaryItemViewHolder, position: Int) {
        holder.display(vesselSummaries[position])
    }

    override fun getItemCount() = vesselSummaries.size
}