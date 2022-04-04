package com.finfrock.transect.data

import androidx.room.Dao
import com.finfrock.transect.model.Transect
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TransectDao {
    @Query("SELECT * FROM transect")
    fun getAll(): List<TransectDb>

    @Insert
    suspend fun insertTransect(transect: TransectDb): Long
}