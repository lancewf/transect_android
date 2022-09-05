package com.finfrock.transect

import android.opengl.Visibility
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.network.TransectApiService
import com.google.android.material.appbar.MaterialToolbar
import kotlinx.android.synthetic.main.saving_progress_activity.*
import javax.inject.Inject

class SavingProgressActivity : AppCompatActivity() {
    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var transectApiService: TransectApiService
    lateinit var dataSource: DataSource
    private lateinit var uploadStatusText: TextView
    private lateinit var retryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MyApplication).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        dataSource = ViewModelProvider(viewModelStore,
            DataSource.FACTORY(database, transectApiService))[DataSource::class.java]
        setContentView(R.layout.saving_progress_activity)

        // Read all the local transects
        // Filter for all the transects local only
        // Start saving the transects one by one
        // After all the transects are saved display a close button
        uploadStatusText = findViewById(R.id.uploadStatus)
        retryButton = findViewById(R.id.retryButton)

        val actionBar = findViewById<MaterialToolbar>(R.id.topAppBar)
        actionBar.setNavigationOnClickListener {
            finish()
        }

        retryButton.setOnClickListener {
           retryButton.visibility = View.INVISIBLE
           uploadStatusText.text = "Saving..."
           runUpdate()
        }

        runUpdate()
    }

    override fun onResume() {
        super.onResume()
        runUpdate()
    }

    private fun runUpdate() {
        val (status, statusText) = dataSource.savaAllRemote()
        statusText.observe(this) { uploadStatusText.text = it }
        status.observe(this) { when (it) {
            DataSource.UploadStatus.SAVING -> {
                // do nothing
            }
            DataSource.UploadStatus.FAILED -> {
                retryButton.visibility = View.VISIBLE
            }
            DataSource.UploadStatus.COMPLETE -> {
                // do nothing
            }
        } }
    }
}