package com.finfrock.transect.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransectDao {
    @Query("SELECT * FROM transect")
    fun getAll(): LiveData<List<TransectDb>>

    @Query("SELECT * FROM transect WHERE id = :id LIMIT 1")
    suspend fun getById(id:String): List<TransectDb>

    @Insert
    suspend fun insertTransect(transect: TransectDb): Long
}