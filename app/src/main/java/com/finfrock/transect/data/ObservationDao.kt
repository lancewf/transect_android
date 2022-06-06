package com.finfrock.transect.data

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface ObservationDao {
    @Query("SELECT * FROM observation WHERE transect_id = :transectId")
    fun getAllLiveData(transectId: String): LiveData<List<ObservationDb>>

    @Query("SELECT * FROM observation WHERE transect_id = :transectId")
    suspend fun getAll(transectId: String): List<ObservationDb>

    /**
     * Insert an object in the database.
     *
     * @param obj the object to be inserted.
     * @return The SQLite row id
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertObservation(observation: ObservationDb): Long

    /**
     * Insert an array of objects in the database.
     *
     * @param obj the objects to be inserted.
     * @return The SQLite row ids
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertObservations(obj: List<ObservationDb>): List<Long>

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    suspend fun update(obj: ObservationDb?)

    /**
     * Update an array of objects from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    suspend fun update(obj: List<ObservationDb?>?)

    /**
     * Delete an object from the database
     *
     * @param obj the object to be deleted
     */
    @Query("DELETE FROM observation WHERE id = :obId")
    suspend fun deleteByObId(obId: String)

    @Transaction
    suspend fun upsert(obj: ObservationDb) {
        val id: Long = insertObservation(obj)
        if (id == -1L) {
            update(obj)
        }
    }

    @Transaction
    suspend fun upsert(objList: List<ObservationDb>) {
        val insertResult: List<Long> = insertObservations(objList)
        val updateList: MutableList<ObservationDb> = ArrayList()
        for (i in insertResult.indices) {
            if (insertResult[i] == -1L) {
                updateList.add(objList[i])
            }
        }
        if (updateList.isNotEmpty()) {
            update(updateList)
        }
    }
}