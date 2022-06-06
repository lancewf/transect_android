package com.finfrock.transect

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.finfrock.transect.adapter.SightingItemAdapter
import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.model.ActiveTransect
import com.finfrock.transect.model.Observation
import com.finfrock.transect.model.ObservationBuilder
import com.finfrock.transect.model.Transect
import com.finfrock.transect.util.CountUpTimer
import com.finfrock.transect.util.LocationProxy
import com.finfrock.transect.util.LocationProxyLike
import com.finfrock.transect.util.MockLocationProxy
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.appbar.MaterialToolbar
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject

class RunningTransectActivity : AppCompatActivity() {
        companion object {
            const val VESSEL_ID = "vesselId"
            const val OBSERVER1_ID = "observer1"
            const val OBSERVER2_ID = "observer2"
            const val BEARING = "bearing"
        }

    @Inject
    lateinit var database: AppDatabase
    lateinit var dataSource: DataSource
    private val observationBuilder = ObservationBuilder()
    private lateinit var transectStart: LocalDateTime
    private lateinit var startLocation: LatLng
    private var vesselId: Int = -1
    private var observer1Id: Int = -1
    private var observer2Id: Int? = null
    private var bearing: Int = -1
    private lateinit var transectId: String
    private lateinit var addSightingButton: Button
    private lateinit var addWeatherButton: Button
    private lateinit var deleteButton: Button
    private lateinit var locationProxy: LocationProxyLike
    private var areControlsLockedDown = false
    private lateinit var toolBar: MaterialToolbar
    private lateinit var sightingAdapter: SightingItemAdapter
    private lateinit var pagerViewer: PagerViewer
    private lateinit var counter: CountUpTimer
    private var resumedObservations:List<Observation> = emptyList()

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted: Map<String, Boolean> ->
            if (isGranted.all{d -> d.value}) {
                postInitialize()
            } else {
                Toast.makeText(this,
                    "Transect can not be created without location access", Toast.LENGTH_LONG).show()
                finish()
            }
        }

    class PagerViewer(private val textView: TextView, private val size: () -> Int) {
        fun updatePage(index: Int) {
            textView.text = "${index + 1} of ${size()}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MyApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        dataSource = ViewModelProvider(viewModelStore, DataSource.FACTORY(database))
            .get(DataSource::class.java)
        setContentView(R.layout.running_transect_activity)

        preInitialize()
        dataSource.backgroundGetActiveTransect{activeTransect:ActiveTransect?, obs:List<Observation> ->
            if(activeTransect != null) {
                vesselId = activeTransect.vesselId
                observer1Id = activeTransect.observer1Id
                observer2Id = activeTransect.observer2Id
                bearing = activeTransect.bearing
                transectId = activeTransect.id
                transectStart = activeTransect.startDate
                startLocation = activeTransect.startLatLon
                resumedObservations = obs
            } else {
                vesselId = intent.extras?.getInt(VESSEL_ID) ?: -1
                observer1Id = intent.extras?.getInt(OBSERVER1_ID) ?: -1
                observer2Id = intent.extras?.getInt(OBSERVER2_ID)
                bearing = intent.extras?.getInt(BEARING) ?: -1
                transectId = UUID.randomUUID().toString()
                transectStart = LocalDateTime.now()
            }

            checkPermissions()
        }
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> {
                postInitialize()
            }
            else -> {
                requestPermissionLauncher.launch(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION))
            }
        }
    }

    private fun preInitialize() {
//        locationProxy = MockLocationProxy()
        locationProxy = LocationProxy(this, LocationServices.getFusedLocationProviderClient(this))

        val recyclerView = findViewById<RecyclerView>(R.id.sighting_view)

        sightingAdapter = SightingItemAdapter(observationBuilder)
        val sightingLayoutManager = LinearLayoutManager(
            this@RunningTransectActivity, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.apply {
            layoutManager = sightingLayoutManager
            adapter = sightingAdapter
        }
        val pagerTextView: TextView = findViewById(R.id.pager_view)
        pagerViewer =
            RunningTransectActivity.PagerViewer(pagerTextView) { -> observationBuilder.size() }

        PagerSnapHelper().attachToRecyclerView(recyclerView)
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

        deleteButton = findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false
        deleteButton.setOnClickListener {
            val selectedIndex = sightingLayoutManager.findFirstVisibleItemPosition()
            deleteObservation(selectedIndex)
        }

        sightingAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(start: Int, count: Int) {
                recyclerView.smoothScrollToPosition(start)
                pagerViewer.updatePage(start)
            }

            override fun onChanged() {
                super.onChanged()

                recyclerView.smoothScrollToPosition(observationBuilder.size() - 1)
                pagerViewer.updatePage(observationBuilder.size() - 1)
            }
        })

        observationBuilder.afterDataChanged {
            updateButtons()
            updateDb()
        }

        addSightingButton = findViewById(R.id.addSightingButton)
        addSightingButton.setOnClickListener { addSightingObservation() }
        addWeatherButton = findViewById(R.id.addWeatherButton)
        addWeatherButton.setOnClickListener { addWeatherObservation() }

        toolBar = findViewById(R.id.topAppBar)

        counter = object: CountUpTimer(1000) {
            override fun onTick(totalSeconds: Long) {
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                toolBar.title = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        }

        toolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_stop -> {
                    counter.stop()
                    val transectStopDate = LocalDateTime.now()
                    locationProxy.getLocation().addOnSuccessListener { transectStopLatLon ->
                        storeTransect(transectStopLatLon, transectStopDate)
                        finish()
                    }
                    true
                }
                else -> false
            }
        }

    }

    private fun postInitialize() {
        val bearingLabel = findViewById<TextView>(R.id.bearingLabel)
        bearingLabel.text = bearing.toString()

        val observer1Label = findViewById<TextView>(R.id.observerName1)
        observer1Label.text = getObserverName(observer1Id)

        val observer2Label = findViewById<TextView>(R.id.observerName2)
        observer2Label.text = getObserverName(observer2Id)

        val vesselLabel = findViewById<TextView>(R.id.vesselSumName)
        vesselLabel.text = getVesselName(vesselId)

        // Not resuming from previous transect
        if (resumedObservations.isEmpty()) {
            addWeatherObservation()
            counter.start()
            getLocation{latLng ->
                startLocation = latLng
                // Save to active
                dataSource.startActiveTransect(
                    ActiveTransect(
                        id = transectId,
                        startDate = transectStart,
                        startLatLon = latLng,
                        vesselId = vesselId,
                        bearing = bearing,
                        observer1Id = observer1Id,
                        observer2Id = observer2Id,
                    )
                )
            }
        } else { // resuming
            sightingAdapter.addAll(resumedObservations)
            counter.start(transectStart.toEpochSecond(ZoneOffset.UTC))
        }
    }

    private fun deleteObservation(selectedIndex: Int) {
        if (observationBuilder.nonEmpty()) {
            val deletedOb = observationBuilder.removeAt(selectedIndex)
            sightingAdapter.notifyItemRemoved(selectedIndex)

            dataSource.deleteObservation(deletedOb.id, transectId)
            when {
                selectedIndex > observationBuilder.size() - 1 ->
                    pagerViewer.updatePage(observationBuilder.size() - 1)
                selectedIndex > 0 -> pagerViewer.updatePage(selectedIndex)
                else -> pagerViewer.updatePage(0)
            }
        }
    }

    private fun addSightingObservation() {
        val newObsId = sightingAdapter.addNewSighting()
        getLocation{ latLng ->
            observationBuilder.updateFromId(newObsId){ obs ->
                obs.location = latLng

                obs
            }
        }
    }

    private fun addWeatherObservation() {
        val newObsId = sightingAdapter.addNewWeatherObservation()
        getLocation{ latLng ->
            observationBuilder.updateFromId(newObsId){ obs ->
                obs.location = latLng

                obs
            }
        }
    }

    private fun getLocation(after: (latLng: LatLng) -> Unit) {
        areControlsLockedDown = true
        toolBar.menu.getItem(1).isVisible = true
        updateButtons()
        locationProxy.getLocation().addOnSuccessListener { latLng ->
            after(latLng)
            areControlsLockedDown = false
            toolBar.menu.getItem(1).isVisible = false
            updateButtons()
        }
    }

    private fun updateDb() {
        if (observationBuilder.isValid() ) {
           dataSource.upsertObservations(observationBuilder.toList(), transectId)
        }
    }

    private fun updateButtons() {
        val obsIsValid = observationBuilder.isValid()
        addSightingButton.isEnabled = obsIsValid && !areControlsLockedDown
        addWeatherButton.isEnabled = obsIsValid && !areControlsLockedDown
        deleteButton.isEnabled = observationBuilder.nonEmpty() && !areControlsLockedDown
    }

    private fun storeTransect(transectStopLatLon: LatLng, transectStopDate: LocalDateTime) {
        dataSource.addTransect(Transect(
            id = transectId,
            startDate = transectStart,
            endDate = transectStopDate,
            startLatLon = startLocation,
            endLatLon = transectStopLatLon,
            vesselId = vesselId,
            observer1Id = observer1Id,
            observer2Id = observer2Id,
            bearing = bearing
        ))
    }

    private fun getObserverName(id: Int?): String {
        val observers = dataSource.loadObservers()
        val observer = observers.find {
            it.id == id
        }
        return observer?.name ?: ""
    }

    private fun getVesselName(id: Int?): String {
        val vessels = dataSource.loadVesselSummaries()
        val vessel = vessels.find {
            it.id == id
        }
        return vessel?.name ?: ""
    }

}