package com.finfrock.transect.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.R
import com.finfrock.transect.TransectSummaryActivity
import com.finfrock.transect.adapter.holder.TransectItemViewHolder
import com.finfrock.transect.model.Transect

class TransectItemAdapter(private val context: Context, private val transects: List<Transect>):
    RecyclerView.Adapter<TransectItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransectItemViewHolder {
        val adapterLayout =
            LayoutInflater.from(parent.context).inflate(
                R.layout.transect_list_item, parent, false)

        val holder = TransectItemViewHolder(adapterLayout)
        holder.setOnClickListener {
            val transect = transects[holder.adapterPosition]

            val intent = Intent(context, TransectSummaryActivity::class.java)

            intent.putExtra(TransectSummaryActivity.TRANSECT_ID, transect.id)

            context.startActivity(intent)
        }

        return holder
    }

    override fun onBindViewHolder(holder: TransectItemViewHolder, position: Int) {
        holder.display(transects[position])
    }

    override fun getItemCount() = transects.size
}