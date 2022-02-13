package com.finfrock.transect

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.adapter.SightingItemAdapter
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.LatLon
import com.finfrock.transect.model.Sighting
import com.finfrock.transect.model.Transect
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.appbar.MaterialToolbar
import java.util.*


class RunningTransectActivity : AppCompatActivity() {
        companion object {
            const val VESSEL_ID = "vesselId"
            const val OBSERVER1_ID = "observer1"
            const val OBSERVER2_ID = "observer2"
            const val BEARING = "bearing"
        }

    private val mutableSightings = mutableListOf<Sighting>()
    private val transectStart = Date()
    private lateinit var startLocation: LatLon
    private var vesselId: Int = -1
    private var observer1Id: Int = -1
    private var observer2Id: Int? = null
    private var bearing: Int = -1


    class PagerViewer(private val layoutManager: LinearLayoutManager,
                      private val textView: TextView, private val size: () -> Int) {
        fun updatePage(index: Int) {
            textView.text = "${index + 1} of ${size()}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.running_transect_activity)
//        val locationProxy = MockLocationProxy()
        val locationProxy = LocationProxy(this, LocationServices.getFusedLocationProviderClient(this))
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
            finish()
            return
        }

        val bundle = intent.getExtras()

        vesselId = bundle?.getInt(VESSEL_ID) ?: -1
        observer1Id = bundle?.getInt(OBSERVER1_ID) ?: -1
        observer2Id = bundle?.getInt(OBSERVER2_ID)
        bearing = bundle?.getInt(BEARING) ?: -1

        val bearingLabel = findViewById<TextView>(R.id.bearingLabel)
        bearingLabel.text = bearing.toString()

        val observer1Label = findViewById<TextView>(R.id.observerName1)
        observer1Label.text = getObserverName(observer1Id)

        val observer2Label = findViewById<TextView>(R.id.observerName2)
        observer2Label.text = getObserverName(observer2Id)

        val vesselLabel = findViewById<TextView>(R.id.vesselName)
        vesselLabel.text = getVesselName(vesselId)

        val recyclerView = findViewById<RecyclerView>(R.id.sighting_view)

        val sightingAdapter = SightingItemAdapter(mutableSightings, locationProxy)
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


        val deleteButton = findViewById<Button>(R.id.deleteButton)
        deleteButton.isEnabled = false
        deleteButton.setOnClickListener {
            val selectedIndex = sightingLayoutManager.findFirstVisibleItemPosition()
            if (mutableSightings.size > 0) {
                mutableSightings.removeAt(selectedIndex)
                sightingAdapter.notifyItemRemoved(selectedIndex)

                when {
                    selectedIndex > mutableSightings.size - 1 -> pagerViewer.updatePage(mutableSightings.size - 1)
                    selectedIndex > 0 -> pagerViewer.updatePage(selectedIndex)
                    else -> pagerViewer.updatePage(0)
                }
            }

            if (mutableSightings.size == 0) {
                deleteButton.isEnabled = false
            }
        }

        val addButton = findViewById<Button>(R.id.addSightingButton)
        addButton.isEnabled = false
        addButton.setOnClickListener {
            sightingAdapter.addNewSighting()
            if (mutableSightings.size > 0) {
                deleteButton.isEnabled = true
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

        toolBar.setOnMenuItemClickListener { menuItem ->
           when (menuItem.itemId) {
               R.id.action_stop -> {
                   counter.stop()
                   val transectStopDate = Date()
                   locationProxy.getLocation().addOnSuccessListener { transectStopLatLon ->
                       storeTransect(transectStopLatLon, transectStopDate)
                       finish()
                   }
                  true
               }
               else -> false
           }
        }

        locationProxy.getLocation().addOnSuccessListener {
            startLocation = it
            addButton.isEnabled = true
        }
    }

    private fun storeTransect(transectStopLatLon: LatLon, transectStopDate: Date) {
        DataSource.addTransect(Transect(
          startDate = transectStart,
            endDate = transectStopDate,
            startLatLon = this.startLocation,
            endLatLon = transectStopLatLon,
            sightings = mutableSightings,
            vesselId = vesselId,
            observer1Id = observer1Id,
            observer2Id = observer2Id,
            bearing = bearing
        ))
    }

    private fun getObserverName(id: Int?): String {
        val observers = DataSource.loadObservers()
        val observer = observers.find {
            it.id == id
        }
        return observer?.name ?: ""
    }

    private fun getVesselName(id: Int?): String {
        val vessels = DataSource.loadVesselSummaries()
        val vessel = vessels.find {
            it.id == id
        }
        return vessel?.name ?: ""
    }

}