package com.finfrock.transect.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ObservationDao {
    @Query("SELECT * FROM observation WHERE transect_id = :transectId")
    fun getAll(transectId: String): List<ObservationDb>

    @Insert
    suspend fun insertObservation(observation: ObservationDb): Long
}