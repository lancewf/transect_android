package com.finfrock.transect

import android.app.Application
import com.finfrock.transect.data.DataSource
import com.finfrock.transect.di.AppComponent
import com.finfrock.transect.di.DaggerAppComponent
import javax.inject.Inject

open class MyApplication: Application() {

    @Inject
    lateinit var dataSource: DataSource

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }


    override fun onCreate() {
        super.onCreate()
        appComponent.inject(this)
    }
}