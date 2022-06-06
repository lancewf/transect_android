package com.finfrock.transect

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.adapter.TransectItemAdapter
import com.finfrock.transect.adapter.VesselSummaryItemAdapter
import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.VesselSummary
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import javax.inject.Inject

class VesselSummaryActivity : AppCompatActivity() {
    companion object {
        const val VESSEL_ID = "vesselId"
    }

    @Inject
    lateinit var database: AppDatabase
    lateinit var dataSource: DataSource

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MyApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)

        dataSource = ViewModelProvider(viewModelStore, DataSource.FACTORY(database))
            .get(DataSource::class.java)
        setContentView(R.layout.vessel_summary_activity)

        val vesselId = intent.extras?.getInt(VESSEL_ID) ?: -1

        val actionBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        actionBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)

        actionBar.setNavigationOnClickListener {
            finish()
        }
        val vesselSummary = getVesselSummary(vesselId)

        if (vesselSummary != null) {
            actionBar.title = vesselSummary.name
        }

        val recyclerView = findViewById<RecyclerView>(R.id.single_vessel_view)

        val transectItemAdapter = TransectItemAdapter(this, dataSource)
        recyclerView.adapter = transectItemAdapter
        recyclerView.setHasFixedSize(true)

        dataSource.getTransectsWithVesselId(vesselId).observe(this){ changedTransects ->
            transectItemAdapter.updateTransects(changedTransects)
        }
    }

    private fun getVesselSummary(vesselId: Int): VesselSummary? {
        return dataSource.loadVesselSummaries().find{ it.id == vesselId}
    }
}