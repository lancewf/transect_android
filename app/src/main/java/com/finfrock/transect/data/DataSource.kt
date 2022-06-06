package com.finfrock.transect.data

import androidx.lifecycle.*
import com.finfrock.transect.model.*
import com.finfrock.transect.model.Observer
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

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
            VesselSummary(id = 1, name = "Kohola", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(id = 2, name = "Ohua", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57"),
            VesselSummary(id = 3, name = "Aloha Kai", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(id = 4, name = "CaneFire II", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57"),
            VesselSummary(id = 5, name = "Kai Kanani", numberOfSightings = 44, numberOfTransects = 11,
                animalsPerKm = 1.32, totalDistanceTraveledKm = 50.93, totalDuration = "1:35:21"),
            VesselSummary(id = 6, name = "Trilogy V", numberOfSightings = 10, numberOfTransects = 4,
                animalsPerKm = 0.5, totalDistanceTraveledKm = 31.93, totalDuration = "1:05:57")
        )
    }

    fun getTransectsWithVesselId(vesselId: Int): LiveData<List<Transect>> {
        return getTransects().map{transects -> transects.map{it.transect}.filter{it.vesselId == vesselId}}
    }

    fun getTransectFromId(transectId: String): LiveData<Transect?>  {
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

    fun addTransect(transect: Transect){
        viewModelScope.launch(Dispatchers.IO) {
            saveTransect(transect)
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

    private fun activeTransectDbtoActiveTransect(activeTransect: ActiveTransectDb): ActiveTransect {
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
        val transectDb: TransectDb = toTransectDb(transect)
        appDatabase.transectDao.insertTransect(transectDb)
        appDatabase.activeTransectDao.deleteAll()
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

    private fun toTransectDb(transect:Transect): TransectDb {
        return TransectDb(
            id = transect.id,
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
            Observer(id = 1, name = "Ed Lyman"),
            Observer(id = 2, name = "Grant Thompson"),
            Observer(id = 3, name = "Jason Moore"),
            Observer(id = 4, name = "Lance"),
            Observer(id = 5, name = "Rachel Finn"),
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
                TransectState(transectDbToTransect(transectDb), true)
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
            activeTransectDbtoActiveTransect(transect)
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

    private fun fakeResume():ActiveTransect {
        val transect = fakeHawaiiTransect()
        return ActiveTransect(
            id = transect.id,
            startDate = transect.startDate,
            startLatLon = transect.startLatLon,
            vesselId = transect.vesselId,
            bearing = transect.bearing,
            observer1Id = transect.observer1Id,
            observer2Id = transect.observer2Id,
        )
    }

    private fun fakeHawaiiTransect():Transect {
        val now = LocalDateTime.now()
        return Transect(
            id = UUID.randomUUID().toString(),
            startDate = now.minusHours(2),
            endDate = now,
            startLatLon = LatLng(20.780584, -156.504399),
            endLatLon = LatLng(20.572826, -156.652441),
            vesselId = 1, bearing = 90,
            observer1Id = 4, observer2Id = 5
        )
    }

    private fun getFakeTransects(): List<TransectState> {
        return listOf(
            TransectState(fakeHawaiiTransect(), true ),
            TransectState(Transect(id = UUID.randomUUID().toString(),
                startDate = LocalDateTime.now(), endDate = LocalDateTime.now(),
                startLatLon = LatLng(0.0, 0.0), endLatLon = LatLng(0.0, 0.0),
                vesselId = 1, bearing = 77,
                observer1Id = 1, observer2Id = 2
            ), true )
        )
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
