package com.finfrock.transect.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.MyApplication
import com.finfrock.transect.R
import com.finfrock.transect.adapter.VesselSummaryItemAdapter
import com.finfrock.transect.data.DataSource
import javax.inject.Inject

class SummaryPageFragment : Fragment() {

    @Inject
    lateinit var dataSource: DataSource

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.summary_frag, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val myDataset = dataSource.loadVesselSummaries()

        val recyclerView = requireView().findViewById<RecyclerView>(R.id.vessel_view)

        recyclerView.adapter = VesselSummaryItemAdapter(requireView().context, myDataset)
        recyclerView.setHasFixedSize(true)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (activity?.application as MyApplication).appComponent.inject(this)
    }
}