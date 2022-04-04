package com.finfrock.transect.di

import android.content.Context
import com.finfrock.transect.data.AppDatabase
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
}