package com.finfrock.transect

import android.app.Application
import com.finfrock.transect.di.AppComponent
import com.finfrock.transect.di.DaggerAppComponent
import com.finfrock.transect.notification.Notification
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

open class MyApplication: Application() {

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }

    @Inject
    lateinit var remoteInitializer: RemoteInitializer

    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)

        MainScope().launch {
            remoteInitializer.remoteInitialLoad()
        }
        Notification.createChannel(this)
    }
}