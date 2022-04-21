package com.finfrock.transect.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ActiveTransectDao {
    @Query("SELECT * FROM active_transect")
    fun getAll(): List<ActiveTransectDb>

    @Insert
    suspend fun insertTransect(transect: ActiveTransectDb): Long

    @Query("DELETE FROM active_transect")
    fun deleteAll()
}