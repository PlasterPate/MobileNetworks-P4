package com.example.thorium.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.thorium.local.database.entity.CellInfoEntity
import com.example.thorium.local.database.entity.LocationEntity

@Database(
    entities = [CellInfoEntity::class, LocationEntity::class],
    version = 4,
    exportSchema = false
)
abstract class CellDatabase : RoomDatabase() {

    abstract val cellDao: CellDao

    abstract val locationDao: LocationDao

    companion object {

        @Volatile
        private var INSTANCE: CellDatabase? = null

        fun getInstance(context: Context): CellDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        CellDatabase::class.java,
                        "cell_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}