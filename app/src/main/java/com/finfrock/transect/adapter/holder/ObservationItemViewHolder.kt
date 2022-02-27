package com.finfrock.transect.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.adapter.OnErrorStatusChangeListener
import com.finfrock.transect.model.Observation

abstract class ObservationItemViewHolder(view: View): RecyclerView.ViewHolder(view) {
    abstract fun display(obs: Observation)

    abstract fun onErrorStatusChanged(listener: (Boolean) -> Unit)
}
