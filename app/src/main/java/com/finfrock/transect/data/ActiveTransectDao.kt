package com.finfrock.transect.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveTransectDao {
    @Query("SELECT * FROM active_transect LIMIT 1")
    fun getFirstLiveData(): LiveData<ActiveTransectDb?>

    @Query("SELECT * FROM active_transect LIMIT 1")
    suspend fun getFirst(): List<ActiveTransectDb>

    @Insert
    suspend fun insertTransect(transect: ActiveTransectDb): Long

    @Query("DELETE FROM active_transect")
    suspend fun deleteAll()
}