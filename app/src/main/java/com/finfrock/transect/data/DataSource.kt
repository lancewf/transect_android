package com.finfrock.transect.data

import android.R
import android.os.Handler
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Transformations.map
import com.finfrock.transect.model.*
import com.finfrock.transect.model.Observer
import com.finfrock.transect.network.RemoteObservation
import com.finfrock.transect.network.RemoteTransect
import com.finfrock.transect.network.TransectApi
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


class DataSource (private val appDatabase:AppDatabase) : ViewModel() {

    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = singleArgViewModelFactory(::DataSource)
    }

    fun loadVesselSummaries(): List<VesselSummary> {
        return listOf(
            VesselSummary(id = "kohola", name = "Kohola", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(id = "ohua", name = "Ohua", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57"),
            VesselSummary(id = "aloha", name = "Aloha Kai", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(id = "canefire", name = "CaneFire II", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57"),
            VesselSummary(id = "kai", name = "Kai Kanani", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(id = "trilogy", name = "Trilogy V", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57")
        )
    }

    fun getRemoteTransects(): LiveData<String> {
        val response = MutableLiveData<String>()
        viewModelScope.launch {
            try {
                val transects = TransectApi.retrofitService.getTransects()
                response.value = "Success: ${transects.size} Remote transects retrieved"
            } catch (e: Exception) {
                response.value = "Failure ${e.message}"
            }
        }

        return response
    }

    fun getTransectsWithVesselId(vesselId: String): LiveData<List<Transect>> {
        return getTransects().map{transects -> transects.map{it.transect}.filter{it.vesselId == vesselId}}
    }

    private fun getTransectFromId(transectId: String): LiveData<Transect?>  {
        return getTransects().map{transects -> transects.map{it.transect}.find{it.id == transectId}}
    }

    fun getTransectWithObservations(transectId: String): LiveData<Pair<Transect?, List<Observation>>> {
        return getTransectFromId(transectId).switchMap { transect ->
            appDatabase.observationDao.getAllLiveData(transectId).map { obs ->
                obs.map { observationDbToObservation(it) }
                    .sortedBy { it.datetime.toEpochSecond(ZoneOffset.UTC) }
            }.map{Pair(transect, it)}
        }
    }

    fun addTransect(transect: Transect, after: () -> Unit){
        viewModelScope.launch {
            saveTransect(transect)
            after()
        }
    }

    fun startActiveTransect(transect:ActiveTransect) {
        viewModelScope.launch(Dispatchers.IO) {
            val transectDb: ActiveTransectDb = toActiveTransectDb(transect)
            appDatabase.activeTransectDao.insertTransect(transectDb)
        }
    }

    fun upsertObservations(obs: List<Observation>, transectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.observationDao.upsert(toObservationDbs(obs, transectId))
        }
    }

    fun deleteObservation(obId: String, transectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            appDatabase.observationDao.deleteByObId(obId)
        }
    }

    fun resumeTransect(callback: (resume:Boolean) -> Unit) {
        viewModelScope.launch {
            callback(fetchActiveTransect() != null)
        }
    }

    fun backgroundTransectWithObservations(transectId: String, callback: (transect:Transect?, obs:List<Observation>) -> Unit) {
        viewModelScope.launch {
            val transect = fetchTransect(transectId)

            val obs = if (transect != null) {
                fetchObservations(transect.id)
            } else {
                emptyList()
            }

            callback(transect, obs)
        }
    }

    fun backgroundGetActiveTransect(callback: (activeTransect:ActiveTransect?, obs:List<Observation>) -> Unit) {
        viewModelScope.launch {
            val activeTransect = fetchActiveTransect()

            val obs = if (activeTransect != null) {
                fetchObservations(activeTransect.id)
            } else {
                emptyList()
            }

            callback(activeTransect, obs)
        }
    }

    private fun activeTransectDbToActiveTransect(activeTransect: ActiveTransectDb): ActiveTransect {
        return ActiveTransect(
            id = activeTransect.id,
            startDate = dbDateToLocalDate(activeTransect.startDate),
            startLatLon = LatLng(activeTransect.startLat, activeTransect.startLon),
            vesselId = activeTransect.vesselId,
            bearing = activeTransect.bearing,
            observer1Id = activeTransect.observer1Id,
            observer2Id = activeTransect.observer2Id
        )
    }

    private suspend fun saveTransect(transect:Transect) {
        val transectDb: TransectDb = toTransectDb(transect, true)
        appDatabase.transectDao.insertTransect(transectDb)
        appDatabase.activeTransectDao.deleteAll()
    }

    enum class UploadStatus {
        SAVING, FAILED, COMPLETE
    }

    fun savaAllRemote():Pair<LiveData<UploadStatus>, LiveData<String>> {
        val statusText = MutableLiveData("")
        val status = MutableLiveData(UploadStatus.SAVING)
        viewModelScope.launch {
            val transectDbs = appDatabase.transectDao.getAllNonLive().
                filter { transectDb -> transectDb.localOnly }

            statusText.value = "Starting to save ${transectDbs.size} Transects"
            val transectPairs = transectDbs.map{ transectDb ->
                val obs = appDatabase.observationDao.getAll(transectDb.id).
                    sortedBy { it.datetime }.
                    map { observationDbToRemoteTransect(it, transectDb.id) }

                Pair(transectToRemoteTransect(transectDb, obs), transectDb)
            }

            for ((index, transectPair) in transectPairs.withIndex()) {
                val (remoteTransect, transectDb) = transectPair
                val errorMessage = saveRemoteAndTest(remoteTransect)
                if (errorMessage != null) {
                    statusText.value = "Error saving Transects: $errorMessage"
                    status.value = UploadStatus.FAILED
                    break
                } else {
                    appDatabase.transectDao.updateLocalOnly(transectDb.id, false)
                    statusText.value = "Saving ${index + 1} of ${transectPairs.size} Transects"
                }
            }

            if (status.value != UploadStatus.FAILED ) {
                statusText.value = "Finished Saving ${transectPairs.size} Transects"
                status.value = UploadStatus.COMPLETE
            }
        }
        return Pair(status, statusText)
    }

    fun hasUnsavedTransects(): LiveData<Boolean> {
        return appDatabase.transectDao.getAll().map{transectDbs -> transectDbs.any{it.localOnly} }
    }

    private suspend fun saveRemoteAndTest(remoteTransect: RemoteTransect):String? {
        try {
            TransectApi.retrofitService.saveTransect(remoteTransect)
        } catch (e: UnknownHostException) {
            return "Internet Not Found"
        } catch (e: Exception) {
            return "Sending transect: ${e.message}"
        }

        try {
           TransectApi.retrofitService.getTransect(remoteTransect.id)
        } catch (e: UnknownHostException) {
            return "Internet Not Found"
        } catch (e: Exception) {
            return "Retrieving transect: ${e.message}"
        }

        return null
    }

    private fun observationDbToRemoteTransect(obDb: ObservationDb, transectId: String): RemoteObservation {
        return RemoteObservation(
           id = obDb.id,
           transectId =  transectId,
            obType = obDb.type.toString(),
            date = obDb.datetime,
            lat = obDb.lat,
            lon = obDb.lon,
            bearing = obDb.bearing,
            count = obDb.count,
            distanceKm = obDb.distanceKm,
            groupType = obDb.groupType.toString(),
            beaufortType = obDb.beaufort.toString(),
            weatherType = obDb.weather.toString()
        )
    }

    private fun transectToRemoteTransect(transect:TransectDb, obs: List<RemoteObservation>):RemoteTransect {
        return RemoteTransect(
           id = transect.id,
           startDate =  transect.startDate,
           endDate =  transect.endDate,
            startLat = transect.startLat,
            startLon = transect.startLon,
            endLat = transect.endLon,
            endLon = transect.endLon,
            vesselId = transect.vesselId,
            bearing = transect.bearing,
            observer1Id = transect.observer1Id,
            observer2Id = transect.observer2Id,
            observations = obs
        )
    }

    private fun toObservationDbs(obs:List<Observation>, transectId: String): List<ObservationDb> {
        return obs.map { ob ->
            toObservationDbs(ob, transectId)
        }
    }
    private fun toObservationDbs(ob: Observation, transectId: String): ObservationDb {
        val obDb = when (ob) {
            is Sighting ->
                ObservationDb(
                    id = ob.id,
                    transectId = transectId,
                    type = 0,
                    datetime = ob.datetime.toEpochSecond(ZoneOffset.UTC).toInt(),
                    lat = ob.location.latitude,
                    lon = ob.location.longitude,
                    count = ob.count,
                    distanceKm = ob.distanceKm,
                    bearing = ob.bearing,
                    groupType = getGroupTypeInt(ob.groupType),
                    beaufort = 0,
                    weather = 0
                )
            is WeatherObservation ->
                ObservationDb(
                    id = ob.id,
                    transectId = transectId,
                    type = 1,
                    datetime = ob.datetime.toEpochSecond(ZoneOffset.UTC).toInt(),
                    lat = ob.location.latitude,
                    lon = ob.location.longitude,
                    count = 0,
                    distanceKm = 0.0,
                    bearing = 0,
                    groupType = 0,
                    beaufort = ob.beaufort,
                    weather = ob.weather
                )
            else -> null
        }

        return obDb!!
    }

    private fun toActiveTransectDb(transect:ActiveTransect): ActiveTransectDb {
        return ActiveTransectDb(
            id = transect.id,
            startDate = transect.startDate.toEpochSecond(ZoneOffset.UTC).toInt(),
            startLat = transect.startLatLon.latitude,
            startLon = transect.startLatLon.longitude,
            vesselId = transect.vesselId,
            bearing = transect.bearing,
            observer1Id = transect.observer1Id,
            observer2Id = transect.observer2Id
        )
    }

    private fun toTransectDb(transect:Transect, localOnly: Boolean): TransectDb {
        return TransectDb(
            id = transect.id,
            localOnly = localOnly,
            startDate = transect.startDate.toEpochSecond(ZoneOffset.UTC).toInt(),
            endDate = transect.endDate.toEpochSecond(ZoneOffset.UTC).toInt(),
            startLat = transect.startLatLon.latitude,
            startLon = transect.startLatLon.longitude,
            endLat = transect.endLatLon.latitude,
            endLon = transect.endLatLon.longitude,
            vesselId = transect.vesselId,
            bearing = transect.bearing,
            observer1Id = transect.observer1Id,
            observer2Id = transect.observer2Id
        )
    }

    fun loadObservers(): List<Observer> {
        return listOf(
            Observer(id = "ED", name = "Ed Lyman"),
            Observer(id = "Grant", name = "Grant Thompson"),
            Observer(id = "Jason", name = "Jason Moore"),
            Observer(id = "lance", name = "Lance"),
            Observer(id = "Rachel", name = "Rachel Finn"),
        )
    }

    private fun dbDateToLocalDate(epoch: Int): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epoch.toLong()), ZoneOffset.UTC)
    }

    private fun transectDbToTransect(transectDb: TransectDb): Transect {
        return Transect(
            id = transectDb.id,
            startDate = dbDateToLocalDate(transectDb.startDate),
            endDate = dbDateToLocalDate(transectDb.endDate),
            startLatLon = LatLng(transectDb.startLat, transectDb.startLon),
            endLatLon = LatLng(transectDb.endLat, transectDb.endLon),
            vesselId = transectDb.vesselId,
            observer1Id = transectDb.observer1Id,
            observer2Id = transectDb.observer2Id,
            bearing = transectDb.bearing,
        )
    }

    private fun getTransects(): LiveData<List<TransectState>> {
        return appDatabase.transectDao.getAll().map{transectDbs ->
            transectDbs.map { transectDb ->
                TransectState(transectDbToTransect(transectDb), transectDb.localOnly)
            }
        }
    }

    private suspend fun fetchTransect(transectId: String): Transect? {
        val transect = appDatabase.transectDao.getById(transectId).firstOrNull()

        return if (transect != null) {
            transectDbToTransect(transect)
        } else {
            null
        }
    }

    private suspend fun fetchActiveTransect(): ActiveTransect? {
        val transect = appDatabase.activeTransectDao.getFirst().firstOrNull()

        return if (transect != null) {
            activeTransectDbToActiveTransect(transect)
        } else {
            null
        }
    }

    private suspend fun fetchObservations(transectId: String):List<Observation> {
        return appDatabase.observationDao.getAll(transectId).map{observationDbToObservation(it)}
            .sortedBy { it.datetime.toEpochSecond(ZoneOffset.UTC) }
    }

    private fun observationDbToObservation(obDb: ObservationDb): Observation {
        return when (obDb.type) {
            0 -> Sighting(
                id = obDb.id,
                datetime = dbDateToLocalDate(obDb.datetime),
                location = LatLng(obDb.lat, obDb.lon),
                count = obDb.count,
                distanceKm = obDb.distanceKm,
                bearing = obDb.bearing,
                groupType = getGroupType(obDb.groupType),
            )
            else -> WeatherObservation(
                id = obDb.id,
                datetime = dbDateToLocalDate(obDb.datetime),
                location = LatLng(obDb.lat, obDb.lon),
                beaufort = obDb.beaufort,
                weather = obDb.weather
            )
        }
    }

    private fun getGroupTypeInt(groupType: GroupType): Int {
        return when(groupType) {
            GroupType.MC -> 0
            GroupType.MCE -> 1
            GroupType.CG -> 2
            else -> 3
        }
    }

    private fun getGroupType(groupTypeId: Int): GroupType {
       return when(groupTypeId) {
           0 -> GroupType.MC
           1 -> GroupType.MCE
           2 -> GroupType.CG
           else -> GroupType.UNKNOWN
       }
    }
}

fun <T : ViewModel, A> singleArgViewModelFactory(constructor: (A) -> T):
            (A) -> ViewModelProvider.NewInstanceFactory {
    return { arg: A ->
        object : ViewModelProvider.NewInstanceFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <V : ViewModel> create(modelClass: Class<V>): V {
                return constructor(arg) as V
            }
        }
    }
}
