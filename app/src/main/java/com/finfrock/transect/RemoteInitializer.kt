package com.finfrock.transect

import com.finfrock.transect.data.AppDatabase
import com.finfrock.transect.data.ObserverDb
import com.finfrock.transect.data.VesselDb
import com.finfrock.transect.network.RemoteObserver
import com.finfrock.transect.network.RemoteVessel
import com.finfrock.transect.network.TransectApi
import com.finfrock.transect.network.TransectApiService
import java.net.UnknownHostException

class RemoteInitializer(val appDatabase: AppDatabase, val transectApiService: TransectApiService) {

    suspend fun remoteInitialLoad() {
        try {
            transectApiService.getObservers().map {
                appDatabase.observerDao.upsert(remoteObserverToObserverDb(it))
            }

            transectApiService.getVessels().map {
                appDatabase.vesselDao.upsert(remoteVesselToVesselDb(it))
            }
        } catch (e: UnknownHostException) {
            //No internet. Use the data from the local DB.
        }
    }

    private fun remoteObserverToObserverDb(observer: RemoteObserver): ObserverDb {
        return ObserverDb(observer.id, observer.name)
    }

    private fun remoteVesselToVesselDb(vessel: RemoteVessel): VesselDb {
        return VesselDb(vessel.id, vessel.name)
    }
}