package com.finfrock.transect.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ObserverDao {
    @Query("SELECT * FROM observer")
    fun getAll(): LiveData<List<ObserverDb>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertObserver(observer: ObserverDb): Long

    @Update
    suspend fun update(obj: ObserverDb?)

    @Transaction
    suspend fun upsert(obj: ObserverDb) {
        val id: Long = insertObserver(obj)
        if (id == -1L) {
            update(obj)
        }
    }
}