package com.example.thorium.local.datasource

import com.example.thorium.ThoriumApplication
import com.example.thorium.data.entity.CellInfoData
import com.example.thorium.local.mappers.toCellInfoData
import com.example.thorium.local.mappers.toLocationEntity
import io.reactivex.Completable
import io.reactivex.Single

object LocationLocalDataSource {

    private val locationDao = ThoriumApplication.cellDatabase.locationDao

    fun insert(cellInfoData: CellInfoData): Completable {
        return locationDao.insert(cellInfoData.toLocationEntity())
    }

    fun getAll(): Single<List<CellInfoData>>{
        return locationDao.getAll().map {
            it.map { locationEntity ->
                locationEntity.toCellInfoData()
            }
        }
    }

    fun deleteAll(): Completable{
        return locationDao.deleteAll()
    }
}