package com.finfrock.transect.data

import com.finfrock.transect.model.*
import com.finfrock.transect.model.Observer
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataSource @Inject constructor(private val appDatabase:AppDatabase) {
    private val transects = mutableListOf<TransectState>()
    private var resumedTransect: ActiveTransect? = null

    init {
//        loadFakeData()
        loadDatabaseData()
        loadResumedTransect()
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

    fun getTransectsWithVesselId(vesselId: Int): List<Transect> {
        return transects.map{it.transect}.filter{it.vesselId == vesselId}
    }

    fun getTransectFromId(transectId: String): Transect?  {
        return transects.map{it.transect}.find{it.id == transectId}
    }

    fun getAllTransects(): List<Transect> {
        return transects.map{ it.transect }
    }

    fun addTransect(transect: Transect){
        transects.add(TransectState(transect, true))
        saveTransect(transect)
    }

    fun hasActiveTransect(): Boolean {
        return resumedTransect != null
    }

    fun getActiveTransect():ActiveTransect? {
        return resumedTransect
    }

    fun startActiveTransect(transect:ActiveTransect) {
        val transectDb: ActiveTransectDb = toActiveTransectDb(transect)
        runBlocking {
            launch {
                appDatabase.activeTransectDao().insertTransect(transectDb)
            }
        }
    }

    fun upsertObservations(obs: List<Observation>, transectId: String) {
        runBlocking {
            launch {
                appDatabase.observationDao().upsert(toObservationDbs(obs, transectId))
            }
        }
    }

    fun deleteObservation(obId: String, transectId: String) {
        runBlocking {
            launch {
                appDatabase.observationDao().deleteByObId(obId)
            }
        }
    }

    private fun loadResumedTransect() {
        val activeTransect = appDatabase.activeTransectDao().getAll().firstOrNull()
        if (activeTransect != null) {
            val obs = fetchObservations(activeTransect.id)
            resumedTransect = ActiveTransect(
                id = activeTransect.id,
                startDate = dbDateToLocalDate(activeTransect.startDate),
                startLatLon = LatLng(activeTransect.startLat, activeTransect.startLon),
                obs = obs,
                vesselId = activeTransect.vesselId,
                bearing = activeTransect.bearing,
                observer1Id = activeTransect.observer1Id,
                observer2Id = activeTransect.observer2Id
            )
        }
//        resumedTransect = fakeResume()
    }

    private fun saveTransect(transect:Transect) {
        val transectDb: TransectDb = toTransectDb(transect)
        runBlocking {
            launch {
                appDatabase.transectDao().insertTransect(transectDb)
                appDatabase.activeTransectDao().deleteAll()
            }
        }
    }

    private fun toObservationDbs(transect:Transect): List<ObservationDb> {
       return toObservationDbs(transect.obs, transect.id)
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

    private fun loadDatabaseData() {
        val databaseTransects = appDatabase.transectDao().getAll().map{transectDb ->
            TransectState(
                Transect(
                    id = transectDb.id,
                    startDate =  dbDateToLocalDate(transectDb.startDate),
                    endDate =  dbDateToLocalDate(transectDb.endDate),
                    startLatLon = LatLng(transectDb.startLat, transectDb.startLon),
                    endLatLon = LatLng(transectDb.endLat, transectDb.endLon),
                    vesselId = transectDb.vesselId,
                    observer1Id = transectDb.observer1Id,
                    observer2Id = transectDb.observer2Id,
                    bearing = transectDb.bearing,
                    obs = fetchObservations(transectDb.id)
                ), true
            )
        }

        transects.addAll(databaseTransects)
    }

    private fun fetchObservations(transectId: String): List<Observation> {
        return appDatabase.observationDao().getAll(transectId).map{obDb ->
          when(obDb.type) {
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
        }.sortedBy { ob -> ob.datetime.toEpochSecond(ZoneOffset.UTC) }
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
            obs = transect.obs,
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
            obs = listOf(
                WeatherObservation(
                    id = UUID.randomUUID().toString(),
                    datetime = now.minusMinutes(83),
                    location = LatLng(20.780584, -156.504399),
                    beaufort = 1,
                    weather = 1
                ),
                Sighting(
                    id = UUID.randomUUID().toString(),
                    datetime = now.minusMinutes(65),
                    location = LatLng(20.730948, -156.529627),
                    count = 1,
                    distanceKm = 0.5,
                    bearing = 0,
                    groupType = GroupType.UNKNOWN
                ),
                WeatherObservation(
                    id = UUID.randomUUID().toString(),
                    datetime = now.plusMinutes(50),
                    location = LatLng(20.711587, -156.536530),
                    beaufort = 1,
                    weather = 1
                ),
                Sighting(
                    id = UUID.randomUUID().toString(),
                    datetime = now.plusMinutes(43),
                    location = LatLng(20.680398, -156.581261),
                    count = 2,
                    distanceKm = 0.5,
                    bearing = 270,
                    groupType = GroupType.UNKNOWN
                ),
                WeatherObservation(
                    id = UUID.randomUUID().toString(),
                    datetime = now.plusMinutes(33),
                    location = LatLng(20.652363, -156.568204),
                    beaufort = 1,
                    weather = 1
                ),
                Sighting(
                    id = UUID.randomUUID().toString(),
                    datetime = now.plusMinutes(5),
                    location = LatLng(20.630308, -156.609860),
                    count = 3,
                    distanceKm = 0.5,
                    bearing = 30,
                    groupType = GroupType.UNKNOWN
                ),
            ),
            vesselId = 1, bearing = 90,
            observer1Id = 4, observer2Id = 5
        )
    }

    private fun loadFakeData() {
        transects.add( TransectState(fakeHawaiiTransect(), true ) )

        transects.add(
            TransectState(Transect(id = UUID.randomUUID().toString(),
                startDate = LocalDateTime.now(), endDate = LocalDateTime.now(),
                startLatLon = LatLng(0.0, 0.0), endLatLon = LatLng(0.0, 0.0),
                obs = emptyList(),
                vesselId = 1, bearing = 77,
                observer1Id = 1, observer2Id = 2
            ), true )
        )
    }
}