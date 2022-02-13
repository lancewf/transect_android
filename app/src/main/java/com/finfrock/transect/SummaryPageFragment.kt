package com.finfrock.transect

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.adapter.VesselSummaryItemAdapter
import com.finfrock.transect.data.DataSource
import com.google.android.material.textfield.TextInputLayout

class SummaryPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.summary_frag, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myDataset = DataSource.loadVesselSummaries()

        val recyclerView = requireView().findViewById<RecyclerView>(R.id.vessel_view)

        recyclerView.adapter = VesselSummaryItemAdapter(requireView().context, myDataset)
        recyclerView.setHasFixedSize(true)
    }
}