package com.finfrock.transect

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.VesselSummary
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar

class VesselSummaryActivity  : AppCompatActivity() {
    companion object {
        const val VESSEL_ID = "vesselId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.vessel_summary_activity)

        val vesselId = intent.extras?.getInt(VESSEL_ID) ?: -1

        val actionBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        actionBar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24)

        actionBar.setNavigationOnClickListener {
            finish()
        }
        val vesselSummary = getVesselSummary(vesselId)
        val nameTextView = findViewById<TextView>(R.id.vesselSumName)

        if (vesselSummary != null) {
            nameTextView.text = vesselSummary.name
        } else {
            nameTextView.text = "None"
        }
    }


    private fun getVesselSummary(vesselId: Int): VesselSummary? {
        return DataSource.loadVesselSummaries().find{ it.id == vesselId}
    }
}