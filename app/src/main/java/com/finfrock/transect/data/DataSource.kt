package com.finfrock.transect.data

import androidx.lifecycle.*
import com.finfrock.transect.model.*
import com.finfrock.transect.model.Observer
import com.finfrock.transect.network.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.nio.file.Files.find
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class DataSource (private val appDatabase:AppDatabase,
                  private val transectApiService: TransectApiService) : ViewModel() {

    companion object {
        /**
         * Factory for creating [MainViewModel]
         *
         * @param arg the repository to pass to [MainViewModel]
         */
        val FACTORY = twoArgViewModelFactory(::DataSource)
    }

    fun getObservers(): LiveData<List<Observer>> {
        return appDatabase.observerDao.getAll().map{ observerDbs ->
            observerDbs.map{observerDbToObserver(it)}
        }
    }

    private fun observerDbToObserver(observerDb: ObserverDb): Observer {
        return Observer(observerDb.id, observerDb.name)
    }

    fun getObserverName(id: String?): LiveData<String> {
        return getObservers().map { observers ->
            val observer = observers.find {
                it.id == id
            }
            observer?.name ?: ""
        }
    }

    fun getVesselName(id: String?): LiveData<String> {
        return getVessels().map { vessels ->
            val vessel = vessels.find {
                it.id == id
            }
            vessel?.name ?: ""
        }
    }

    fun getVessels(): LiveData<List<Vessel>> {
        return appDatabase.vesselDao.getAll().map{ vesselDbs ->
            vesselDbs.map{vesselDbToVessel(it)}
        }
    }

    fun getVesselSummaries(): LiveData<List<VesselSummary>> {
        return appDatabase.vesselDao.getAll().map{ vesselDbs ->
            vesselDbs.map{vesselDbToVesselSummary(it)}
        }
    }

    fun getVesselSummary(vesselId: String): LiveData<VesselSummary?> {
        return appDatabase.vesselDao.getAll().map{ vesselDbs ->  vesselDbs.find{ it.id == vesselId}}.map{
            if (it != null) {
                vesselDbToVesselSummary(it)
            } else {
                null
            }
        }
    }

    private fun vesselDbToVessel(vesselDb: VesselDb): Vessel {
        return Vessel(name = vesselDb.name, id = vesselDb.id)
    }

    private fun vesselDbToVesselSummary(vesselDb: VesselDb): VesselSummary {
        val animalsPerKm = if(vesselDb.totalDistanceOfAllTransectsKm > 0)
            vesselDb.numberOfAnimals /vesselDb.totalDistanceOfAllTransectsKm
        else 0.0
        return VesselSummary(
            id = vesselDb.id, name = vesselDb.name, numberOfSightings = vesselDb.numberOfSightings,
            numberOfTransects = vesselDb.numberOfTransects, animalsPerKm = animalsPerKm,
            totalDistanceTraveledKm = vesselDb.totalDistanceOfAllTransectsKm,
            totalDuration = vesselDb.totalDurationOfAllTransectsSec
        )
    }

    fun getTransectsWithVesselId(vesselId: String): LiveData<List<Transect>> {
        return getTransects().map{transects -> transects.map{it.transect}.filter{it.vesselId == vesselId}}
    }

    private fun getTransectFromId(transectId: String): LiveData<Transect?>  {
        return getTransects().map{transects -> transects.map{it.transect}.find{it.id == transectId}}
    }

    fun getAnimalCountForTransect(transectId: String): LiveData<Int> {
        return appDatabase.observationDao.getAllLiveData(transectId).map{ obs ->
            obs.filter{ob -> ob.type == 0}.sumOf { ob -> ob.count }
        }
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
            transectApiService.saveTransect(remoteTransect)
        } catch (e: UnknownHostException) {
            return "Internet Not Found"
        } catch (e: Exception) {
            return "Sending transect: ${e.message}"
        }

        try {
            transectApiService.getTransect(remoteTransect.id)
        } catch (e: UnknownHostException) {
            return "Internet Not Found"
        } catch (e: Exception) {
            return "Retrieving transect: ${e.message}"
        }

        return null
    }

    private fun observationDbToRemoteTransect(obDb: ObservationDb, transectId: String): RemoteObservation {
        return if(obDb.type == 0){
            RemoteObservation(
                id = obDb.id,
                transectId =  transectId,
                obType = observationDbTypeToRemote(obDb),
                date = obDb.datetime,
                lat = obDb.lat,
                lon = obDb.lon,
                bearing = obDb.bearing,
                count = obDb.count,
                distanceKm = obDb.distanceKm,
                groupType = observationDbGroupTypeToRemote(obDb),
                beaufortType = null,
                weatherType = null
            )
        } else {
            RemoteObservation(
                id = obDb.id,
                transectId = transectId,
                obType = observationDbTypeToRemote(obDb),
                date = obDb.datetime,
                lat = obDb.lat,
                lon = obDb.lon,
                bearing = null,
                count = null,
                distanceKm = null,
                groupType = null,
                beaufortType = observationDbBeaufortTypeToRemote(obDb),
                weatherType = observationDbWeatherTypeToRemote(obDb)
            )
        }
    }

    private fun observationDbTypeToRemote(obDb: ObservationDb): String {
        return when(obDb.type) {
           0 -> "Sighting"
           1 -> "Weather"
           else -> "Not Known"
        }
    }

    private fun observationDbBeaufortTypeToRemote(obDb: ObservationDb): String {
        return when(obDb.beaufort) {
            0 -> "0_calm"
            1 -> "1_1-3kts_ripples"
            2 -> "2_4-6kts_sm_wavelets"
            3 -> "3_7-10kts_lg_wavelets"
            4 -> "4_11-16kts_some_wh_caps"
            5 -> "5_17-21kts_many_wh_caps"
            else -> ">5"
        }
    }

    private fun observationDbWeatherTypeToRemote(obDb: ObservationDb): String {
        return when(obDb.weather) {
            0 -> "sunny"
            1 -> "partly_sunny"
            2 -> "overcast"
            3 -> "scattered_showers"
            4 -> "steady_showers"
            5 -> "squalls"
            6 -> "hard_rain"
            7 -> "haze_vog"
            else -> "unknown"
        }
    }

    private fun observationDbGroupTypeToRemote(obDb: ObservationDb): String {
        return when(obDb.groupType) {
            0 -> "MC"
            1 -> "MCE"
            2 -> "GC"
            else -> "Unknown"
        }
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

    private fun getGroupTypeInt(groupType: GroupType): Int {
        return when(groupType) {
            GroupType.MC -> 0
            GroupType.MCE -> 1
            GroupType.CG -> 2
            else -> 3
        }
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


    private fun getGroupType(groupTypeId: Int): GroupType {
       return when(groupTypeId) {
           0 -> GroupType.MC
           1 -> GroupType.MCE
           2 -> GroupType.CG
           else -> GroupType.UNKNOWN
       }
    }
}

fun <T : ViewModel, A, B> twoArgViewModelFactory(constructor: (A, B) -> T):
            (A, B) -> ViewModelProvider.NewInstanceFactory {
    return { a: A, b: B ->
        object : ViewModelProvider.NewInstanceFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <V : ViewModel> create(modelClass: Class<V>): V {
                return constructor(a, b) as V
            }
        }
    }
}
