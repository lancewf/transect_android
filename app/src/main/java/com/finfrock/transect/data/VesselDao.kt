package com.finfrock.transect.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface VesselDao {
    @Query("SELECT * FROM vessel")
    fun getAll(): LiveData<List<VesselDb>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVessel(vessel: VesselDb): Long

    @Update
    suspend fun update(vessel: VesselDb?)

    @Transaction
    suspend fun upsert(vessel: VesselDb) {
        val id: Long = insertVessel(vessel)
        if (id == -1L) {
            update(vessel)
        }
    }
}
