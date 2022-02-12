package com.finfrock.transect

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.adapter.SightingItemAdapter
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.Sighting
import com.google.android.material.appbar.MaterialToolbar
import java.util.*

class RunningTransectActivity : AppCompatActivity() {
        companion object {
            const val VESSEL_ID = "vesselId"
            const val OBSERVER1_ID = "observer1"
            const val OBSERVER2_ID = "observer2"
            const val BEARING = "bearing"
        }

    class PagerViewer(private val layoutManager: LinearLayoutManager,
                      private val textView: TextView, private val size: () -> Int) {
        fun updatePage(index: Int) {
            textView.text = "${index + 1} of ${size()}"
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.running_transect_activity)

        val bundle = intent.getExtras()

        val vesselId = bundle?.getInt(VESSEL_ID)
        val observer1Id = bundle?.getInt(OBSERVER1_ID)
        val observer2Id = bundle?.getInt(OBSERVER2_ID)
        val bearing = bundle?.getInt(BEARING)

        val bearingLabel = findViewById<TextView>(R.id.bearingLabel)
        bearingLabel.text = bearing.toString()


        val observer1Label = findViewById<TextView>(R.id.observerName1)
        observer1Label.text = getObserverName(observer1Id)

        val observer2Label = findViewById<TextView>(R.id.observerName2)
        observer2Label.text = getObserverName(observer2Id)

        val vesselLabel = findViewById<TextView>(R.id.vesselName)
        vesselLabel.text = getVesselName(vesselId)

        val recyclerView = findViewById<RecyclerView>(R.id.sighting_view)

        val mutableSightings = mutableListOf<Sighting>()

        val sightingAdapter = SightingItemAdapter(baseContext, mutableSightings)
        val sightingLayoutManager = LinearLayoutManager(this@RunningTransectActivity, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.apply {
            layoutManager = sightingLayoutManager
            adapter = sightingAdapter
        }
        val pagerTextView: TextView = findViewById(R.id.pager_view)
        val pagerViewer = PagerViewer(sightingLayoutManager, pagerTextView) { -> mutableSightings.size }

        PagerSnapHelper().attachToRecyclerView(recyclerView)
        // use the below Snap Helper to scroll more than one item at a time.
//        LinearSnapHelper().attachToRecyclerView(recyclerView)
//        recyclerView.addItemDecoration(LinePagerIndicatorDecoration())
        recyclerView.setHasFixedSize(true)

        pagerViewer.updatePage(-1)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == 0) {
                    pagerViewer.updatePage(sightingLayoutManager.findFirstVisibleItemPosition())
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        })

        sightingAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(start: Int, count: Int) {
                recyclerView.smoothScrollToPosition(start +1)
                pagerViewer.updatePage(start +1)
            }
        })

        val addButton = findViewById<Button>(R.id.addSightingButton)
        addButton.setOnClickListener {
            sightingAdapter.addNewSighting()
        }
        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.setOnClickListener {
            val selectedIndex = sightingLayoutManager.findFirstVisibleItemPosition()
            if (mutableSightings.size != 1) {
                mutableSightings.removeAt(selectedIndex)
                sightingAdapter.notifyItemRemoved(selectedIndex)

                when {
                    selectedIndex > mutableSightings.size - 1 -> pagerViewer.updatePage(mutableSightings.size - 1)
                    selectedIndex > 0 -> pagerViewer.updatePage(selectedIndex)
                    else -> pagerViewer.updatePage(0)
                }
            }
        }

        val toolBar = findViewById<MaterialToolbar>(R.id.topAppBar)

        val counter = object: CountUpTimer(1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                toolBar.setTitle(String.format("%02d:%02d:%02d", hours, minutes, seconds))
            }
        }
        counter.start()

        toolBar.setOnMenuItemClickListener {
           when (it.itemId) {
               R.id.action_stop -> {
                  counter.stop()
                   finish()
                  true
               }
               else -> false
           }
        }
    }

    private fun createEmptySighting(): Sighting {
        return Sighting(datetime = Date())
    }

    private fun getObserverName(id: Int?): String {
        val observers = DataSource().loadObservers()
        val observer = observers.find {
            it.id == id
        }
        return if (observer == null) {
            ""
        } else {
            observer!!.name
        }
    }

    private fun getVesselName(id: Int?): String {
        val vessels = DataSource().loadVesselSummaries()
        val vessel = vessels.find {
            it.id == id
        }
        return if (vessel == null) {
            ""
        } else {
            vessel!!.name
        }
    }

}