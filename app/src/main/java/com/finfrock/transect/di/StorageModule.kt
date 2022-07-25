package com.finfrock.transect.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.finfrock.transect.RemoteInitializer
import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.DataSource
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class StorageModule {

    @Singleton
    @Provides
    fun providesAppData(context: Context):AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun providesRemoteInitializer(appDatabase: AppDatabase): RemoteInitializer {
        return RemoteInitializer(appDatabase)
    }
}