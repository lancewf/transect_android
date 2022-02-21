package com.finfrock.transect.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.R
import com.finfrock.transect.TransectSummaryActivity
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.Transect

class TransectItemAdapter(private val context: Context, private val transects: List<Transect>):
    RecyclerView.Adapter<TransectItemAdapter.ItemViewHolder>() {

    class ItemViewHolder(private val view: View): RecyclerView.ViewHolder(view) {
        val sightingCountTextView: TextView = view.findViewById(R.id.sightingCount)
        val observer1NameTextView: TextView = view.findViewById(R.id.observerName1)
        val observer2NameTextView: TextView = view.findViewById(R.id.observerName2)
        val bearingTextView: TextView = view.findViewById(R.id.bearingLabel)
        val container: ConstraintLayout = view.findViewById(R.id.transect_list_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(R.layout.transect_list_item, parent, false)

        val holder = ItemViewHolder(adapterLayout)
        holder.container.setOnClickListener {
            val transect = transects[holder.adapterPosition]

            val intent = Intent(context, TransectSummaryActivity::class.java)

            intent.putExtra(TransectSummaryActivity.TRANSECT_ID, transect.id.toString())

            context.startActivity(intent)
        }

        return holder
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val transect = transects[position]

        val observer1Name = getObserverName(transect.observer1Id)
        val observer2Name = getObserverName(transect.observer2Id)

        holder.sightingCountTextView.text = transect.obs.size.toString()
        holder.observer1NameTextView.text = observer1Name
        holder.observer2NameTextView.text = observer2Name
        holder.bearingTextView.text = transect.bearing.toString()
    }

    override fun getItemCount() = transects.size

    private fun getObserverName(id: Int?): String {
        val observers = DataSource.loadObservers()
        val observer = observers.find {
            it.id == id
        }
        return observer?.name ?: ""
    }
}