package com.finfrock.transect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class TransectSummaryActivity: AppCompatActivity()  {
    companion object {
        const val TRANSECT_ID = "transectId"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.transect_summary_activity)
    }
}