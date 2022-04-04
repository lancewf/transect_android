package com.finfrock.transect.di

import android.content.Context
import com.finfrock.transect.MyApplication
import com.finfrock.transect.RunningTransectActivity
import com.finfrock.transect.TransectSummaryActivity
import com.finfrock.transect.VesselSummaryActivity
import com.finfrock.transect.view.StartPageFragment
import com.finfrock.transect.view.SummaryPageFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [StorageModule::class])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun inject(activity: MyApplication)

    fun inject(activity: RunningTransectActivity)
    fun inject(activity: TransectSummaryActivity)
    fun inject(activity: VesselSummaryActivity)
    fun inject(fragment: StartPageFragment)
    fun inject(fragment: SummaryPageFragment)
}
