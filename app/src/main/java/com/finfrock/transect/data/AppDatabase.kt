package com.finfrock.transect.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TransectDb::class, ObservationDb::class, ActiveTransectDb::class, ObserverDb::class, VesselDb::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract val transectDao: TransectDao
    abstract val observationDao: ObservationDao
    abstract val observerDao: ObserverDao
    abstract val vesselDao: VesselDao
    abstract val activeTransectDao: ActiveTransectDao

    companion object {

        // For Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "transect")
                .build()
        }
    }
}

