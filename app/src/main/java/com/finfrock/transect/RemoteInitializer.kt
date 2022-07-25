package com.finfrock.transect

import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.ObserverDb
import com.finfrock.transect.network.RemoteObserver
import com.finfrock.transect.network.TransectApi

class RemoteInitializer(val appDatabase: AppDatabase) {

    suspend fun remoteInitialLoad() {
        val obs = TransectApi.retrofitService.getObservers()
        obs.map {
            appDatabase.observerDao.upsert(remoteObserverToObserverDb(it))
        }
    }

    private fun remoteObserverToObserverDb(observer: RemoteObserver): ObserverDb {
        return ObserverDb(observer.id, observer.name)
    }
}