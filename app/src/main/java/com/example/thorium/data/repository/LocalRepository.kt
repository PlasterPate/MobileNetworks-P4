package com.example.thorium.data.repository

import com.example.thorium.data.entity.CellInfoData
import com.example.thorium.local.datasource.CellLocalDataSource
import com.example.thorium.local.datasource.LocationLocalDataSource
import io.reactivex.Completable
import io.reactivex.Single

object LocalRepository {

    // CellInfo Entity

    fun insertCellInfo(cellInfoData: CellInfoData): Completable{
        return CellLocalDataSource.insertCellInfo(cellInfoData)
    }

    fun getAllCellInfo(): Single<List<CellInfoData>>{
        return CellLocalDataSource.getAll()
    }

    fun deleteAllCellInfo(): Completable{
        return CellLocalDataSource.deleteAll()
    }


    // Location Entity

    fun insertLocation(cellInfoData: CellInfoData): Completable{
        return LocationLocalDataSource.insert(cellInfoData)
    }

    fun getAllLocations(): Single<List<CellInfoData>>{
        return LocationLocalDataSource.getAll()
    }

    fun deleteAllLocation(): Completable{
        return LocationLocalDataSource.deleteAll()
    }
}