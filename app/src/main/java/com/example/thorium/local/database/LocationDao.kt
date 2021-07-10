package com.example.thorium.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.thorium.local.database.entity.LocationEntity
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface LocationDao {

    @Insert
    fun insert(locationEntity: LocationEntity): Completable

    @Query("SELECT * from location_table")
    fun getAll() : Single<List<LocationEntity>>

    @Query("DELETE from location_table")
    fun deleteAll(): Completable
}