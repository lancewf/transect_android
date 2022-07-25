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

class TransectItemViewHolder(val view: View, private val lifecycleOwner: LifecycleOwner, val dataSource: DataSource): RecyclerView.ViewHolder(view) {
    private val sightingCountTextView: TextView = view.findViewById(R.id.sightingCount)
    private val observer1NameTextView: TextView = view.findViewById(R.id.observerName1)
    private val observer2NameTextView: TextView = view.findViewById(R.id.observerName2)
    private val bearingTextView: TextView = view.findViewById(R.id.bearingLabel)
    private val container: ConstraintLayout = view.findViewById(R.id.transect_list_container)

    fun display(transect: Transect) {
        dataSource.getObserverName(transect.observer1Id).observe(lifecycleOwner) {
                observer1NameTextView.text = it
            }

        dataSource.getObserverName(transect.observer2Id).observe(lifecycleOwner) {
                observer2NameTextView.text = it
            }

        sightingCountTextView.text = "77"
        bearingTextView.text = transect.bearing.toString()
    }

    fun setOnClickListener(listener: View.OnClickListener ) {
        container.setOnClickListener(listener)
    }
}