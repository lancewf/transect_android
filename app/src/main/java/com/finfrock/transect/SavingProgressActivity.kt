package com.finfrock.transect

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.network.TransectApiService
import com.finfrock.transect.notification.Notification
import com.google.android.material.appbar.MaterialToolbar
import javax.inject.Inject

class SavingProgressActivity : AppCompatActivity() {
    @Inject
    lateinit var database: AppDatabase

    @Inject
    lateinit var transectApiService: TransectApiService
    lateinit var dataSource: DataSource
    private lateinit var uploadStatusText: TextView
    private lateinit var uploadStatusTitleText: TextView
    private lateinit var retryButton: Button
    private val unsavedTransectNotificationId = 77

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
        uploadStatusTitleText = findViewById(R.id.uploadStatusTitle)
        uploadStatusTitleText.text = "Saving Transects"

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
                val builder = Notification.createNotificationBuilder(this)
                with(NotificationManagerCompat.from(this)) {
                    notify(unsavedTransectNotificationId, builder.build())
                }
                uploadStatusTitleText.text = "Warning"
                uploadStatusText.text = "Transect(s) are saved to your phone but not uploaded to server.\n\nPlease return to this app when internet is available and click the 'disk icon' to upload your transect(s) to the server. "
            }
            DataSource.UploadStatus.COMPLETE -> {
                with(NotificationManagerCompat.from(this)) {
                    cancel(unsavedTransectNotificationId)
                }
                uploadStatusTitleText.text = "Transect(s) successfully saved to server"
            }
        } }
    }
}