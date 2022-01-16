package com.finfrock.transect

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class RunningTransectActivity : AppCompatActivity() {
        companion object {
            const val VESSEL_ID = "vesselId"
            const val OBSERVER1_ID = "observer1"
            const val OBSERVER2_ID = "observer2"
            const val BEARING = "bearing"
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.running_transect_activity)
    }
}