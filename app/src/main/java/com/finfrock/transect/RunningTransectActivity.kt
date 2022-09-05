package com.finfrock.transect

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
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
import com.finfrock.transect.model.*
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
    private var vesselId: String = ""
    private var observer1Id: String = ""
    private var observer2Id: String? = null
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
    private var resumed = false

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

    class PagerViewer(private val textView: TextView,
                      private val observationBuilder: ObservationBuilder) {
        fun updatePage(overallIndex: Int) {

            val label = when {
                overallIndex < 0 -> ""
                observationBuilder.isSightingAt(overallIndex) -> {
                    val size = observationBuilder.getSightingSize()
                    val sightingIndex = observationBuilder.getSightingIndex(overallIndex)
                    "Sighting ${sightingIndex + 1} of $size"
                }
                observationBuilder.isWeatherAt(overallIndex) -> {
                    val size = observationBuilder.getWeatherSize()
                    val weatherIndex = observationBuilder.getWeatherIndex(overallIndex)
                    "Weather ${weatherIndex  + 1} of $size"
                }
                else -> ""
            }

            textView.text = label
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
                resumed = true
            } else {
                vesselId = intent.extras?.getString(VESSEL_ID)!!
                observer1Id = intent.extras?.getString(OBSERVER1_ID)!!
                observer2Id = intent.extras?.getString(OBSERVER2_ID)
                bearing = intent.extras?.getInt(BEARING) ?: -1
                transectId = UUID.randomUUID().toString()
                transectStart = LocalDateTime.now()
                resumed = false
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
        val pagerTextView: TextView = findViewById(R.id.active_record_text)
        pagerViewer = PagerViewer(pagerTextView, observationBuilder)

        PagerSnapHelper().attachToRecyclerView(recyclerView)
        recyclerView.setHasFixedSize(true)

        pagerViewer.updatePage(-1)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == 0) {
                    val page = sightingLayoutManager.findFirstVisibleItemPosition()
                    pagerViewer.updatePage(page)
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {}
        })

        deleteButton = findViewById(R.id.deleteButton)
        deleteButton.isEnabled = false
        deleteButton.setOnClickListener {
            val alert: AlertDialog.Builder = AlertDialog.Builder( this )
            alert.setTitle("Warning")
            alert.setMessage("Confirm Deletion")
            alert.setPositiveButton("Yes"
            ) { dialog, _ ->
                val selectedIndex = sightingLayoutManager.findFirstVisibleItemPosition()
                deleteObservation(selectedIndex)
                dialog.dismiss()
            }

            alert.setNegativeButton("No") { dialog, _ -> dialog.dismiss() }

            alert.show()
        }

        sightingAdapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(start: Int, count: Int) {
                recyclerView.smoothScrollToPosition(start)
                pagerViewer.updatePage(start)
            }

            override fun onChanged() {
                super.onChanged()
                val page = observationBuilder.size() - 1
                recyclerView.smoothScrollToPosition(page)
                pagerViewer.updatePage(page)
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


        val timerTextView:TextView = findViewById(R.id.timer)
        counter = object: CountUpTimer(1000) {
            override fun onTick(totalSeconds: Long) {
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60
                timerTextView.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }
        }

        toolBar = findViewById(R.id.topAppBar)
        toolBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_stop -> {
                    counter.stop()
                    val transectStopDate = LocalDateTime.now()
                    locationProxy.getLocation().addOnSuccessListener { transectStopLatLon ->
                        val transect = createFinishedTransect(transectStopLatLon, transectStopDate)
                        dataSource.addTransect(transect){
                            startSavingProgressActivity()
                            finish()
                        }
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
        dataSource.getObserverName(observer1Id).observe(this) {
            observer1Label.text = it
        }

        val observer2Label = findViewById<TextView>(R.id.observerName2)
        dataSource.getObserverName(observer2Id).observe(this) {
            observer2Label.text = it
        }

        val vesselLabel = findViewById<TextView>(R.id.vesselSumName)
        dataSource.getVesselName(vesselId).observe(this){
            vesselLabel.text = it
        }

        // Not resuming from previous transect
        if (!resumed) {
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
            if( resumedObservations.isEmpty() ) {
                addWeatherObservation()
            }
        }
    }

    private fun deleteObservation(selectedIndex: Int) {
        if (observationBuilder.nonEmpty()) {
            val deletedOb = observationBuilder.removeAt(selectedIndex)
            sightingAdapter.notifyItemRemoved(selectedIndex)

            dataSource.deleteObservation(deletedOb.id, transectId)
            when {
                selectedIndex > observationBuilder.size() - 1 -> {
                    val page = observationBuilder.size() - 1
                    pagerViewer.updatePage(page)
                }
                selectedIndex > 0 -> {
                    pagerViewer.updatePage(selectedIndex)
                }
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

    private fun createFinishedTransect(
        transectStopLatLon: LatLng, transectStopDate: LocalDateTime): Transect {
        return Transect(
            id = transectId,
            startDate = transectStart,
            endDate = transectStopDate,
            startLatLon = startLocation,
            endLatLon = transectStopLatLon,
            vesselId = vesselId,
            observer1Id = observer1Id,
            observer2Id = observer2Id,
            bearing = bearing
        )
    }

    private fun startSavingProgressActivity() {
        val intent = Intent(this, SavingProgressActivity::class.java)

        this.startActivity(intent)
    }
}