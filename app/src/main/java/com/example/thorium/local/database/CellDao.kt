package com.example.thorium.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thorium.data.entity.CellInfoData
import com.example.thorium.local.database.entity.CellInfoEntity
import io.reactivex.Completable
import io.reactivex.Single


@Dao
interface CellDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(cellInfoEntity: CellInfoEntity): Completable

    @Query("SELECT * from cell_info_table")
    fun getAll(): Single<List<CellInfoEntity>>
    
    @Query("DELETE from cell_info_table")
    fun deleteAll(): Completable
}