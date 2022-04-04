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

    init {
//        loadFakeData()
        loadDatabaseData()
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

    private fun saveTransect(transect:Transect) {
        val transectDb: TransectDb = toTransectDb(transect)
        val observationDbs: List<ObservationDb> = toObservationDbs(transect)
        runBlocking {
            launch {
                appDatabase.transectDao().insertTransect(transectDb)
                observationDbs.forEach{
                    appDatabase.observationDao().insertObservation(it)
                }
            }
        }
    }

    private fun toObservationDbs(transect:Transect): List<ObservationDb> {
        return transect.obs.mapNotNull { ob ->
            when (ob) {
                is Sighting ->
                    ObservationDb(
                        id = ob.id,
                        transectId = transect.id,
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
                        transectId = transect.id,
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
        }
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

    private fun loadDatabaseData() {
        val databaseTransects = appDatabase.transectDao().getAll().map{transectDb ->
            TransectState(
                Transect(
                    id = transectDb.id,
                    startDate =  LocalDateTime.ofInstant(Instant.ofEpochSecond(transectDb.startDate.toLong()), ZoneOffset.UTC),
                    endDate =  LocalDateTime.ofInstant(Instant.ofEpochSecond(transectDb.endDate.toLong()), ZoneOffset.UTC),
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
                  datetime = LocalDateTime.ofInstant(Instant.ofEpochSecond(obDb.datetime.toLong()), ZoneOffset.UTC),
                  location = LatLng(obDb.lat, obDb.lon),
                  count = obDb.count,
                  distanceKm = obDb.distanceKm,
                  bearing = obDb.bearing,
                  groupType = getGroupType(obDb.groupType),
              )
              else -> WeatherObservation(
                  id = obDb.id,
                  datetime = LocalDateTime.ofInstant(Instant.ofEpochSecond(obDb.datetime.toLong()), ZoneOffset.UTC),
                  location = LatLng(obDb.lat, obDb.lon),
                  beaufort = obDb.beaufort,
                  weather = obDb.weather
              )
          }
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

    private fun loadFakeData() {
        val now = LocalDateTime.now()
        transects.add(
            TransectState(Transect(
                id = UUID.randomUUID().toString(),
                startDate = now,
                endDate = now.plusHours(2),
                startLatLon = LatLng(20.780584, -156.504399),
                endLatLon = LatLng(20.572826, -156.652441),
                obs = listOf(
                    WeatherObservation(
                        id = UUID.randomUUID().toString(),
                        datetime = now,
                        location = LatLng(20.780584, -156.504399),
                        beaufort = 1,
                        weather = 1
                    ),
                    Sighting(
                        id = UUID.randomUUID().toString(),
                        datetime = now.plusMinutes(33),
                        location = LatLng(20.730948, -156.529627),
                        count = 1,
                        distanceKm = 0.5,
                        bearing = 90,
                        groupType = GroupType.UNKNOWN
                    ),
                    WeatherObservation(
                        id = UUID.randomUUID().toString(),
                        datetime = now.plusMinutes(43),
                        location = LatLng(20.711587, -156.536530),
                        beaufort = 1,
                        weather = 1
                    ),
                    Sighting(
                        id = UUID.randomUUID().toString(),
                        datetime = now.plusMinutes(50),
                        location = LatLng(20.680398, -156.581261),
                        count = 2,
                        distanceKm = 0.5,
                        bearing = 90,
                        groupType = GroupType.UNKNOWN
                    ),
                    WeatherObservation(
                        id = UUID.randomUUID().toString(),
                        datetime = now.plusMinutes(65),
                        location = LatLng(20.652363, -156.568204),
                        beaufort = 1,
                        weather = 1
                    ),
                    Sighting(
                        id = UUID.randomUUID().toString(),
                        datetime = now.plusMinutes(83),
                        location = LatLng(20.630308, -156.609860),
                        count = 3,
                        distanceKm = 0.5,
                        bearing = 90,
                        groupType = GroupType.UNKNOWN
                    ),
                ),
                vesselId = 1, bearing = 90,
                observer1Id = 4, observer2Id = 5
            ), true )
        )

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