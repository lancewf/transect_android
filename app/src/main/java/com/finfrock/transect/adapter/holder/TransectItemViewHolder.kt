package com.finfrock.transect.adapter.holder

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.R
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.Transect

class TransectItemViewHolder(view: View, val dataSource: DataSource): RecyclerView.ViewHolder(view) {
    private val sightingCountTextView: TextView = view.findViewById(R.id.sightingCount)
    private val observer1NameTextView: TextView = view.findViewById(R.id.observerName1)
    private val observer2NameTextView: TextView = view.findViewById(R.id.observerName2)
    private val bearingTextView: TextView = view.findViewById(R.id.bearingLabel)
    private val container: ConstraintLayout = view.findViewById(R.id.transect_list_container)

    fun display(transect: Transect) {
        val observer1Name = getObserverName(transect.observer1Id)
        val observer2Name = getObserverName(transect.observer2Id)

        sightingCountTextView.text = "77"
        observer1NameTextView.text = observer1Name
        observer2NameTextView.text = observer2Name
        bearingTextView.text = transect.bearing.toString()
    }

    fun setOnClickListener(listener: View.OnClickListener ) {
        container.setOnClickListener(listener)
    }

    private fun getObserverName(id: String?): String {
        val observers = dataSource.loadObservers()
        val observer = observers.find {
            it.id == id
        }
        return observer?.name ?: ""
    }
}