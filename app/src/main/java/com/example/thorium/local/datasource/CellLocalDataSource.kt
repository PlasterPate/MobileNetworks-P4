package com.example.thorium.local.datasource

import com.example.thorium.ThoriumApplication
import com.example.thorium.data.entity.CellInfoData
import com.example.thorium.local.mappers.toCellInfoData
import com.example.thorium.local.mappers.toCellInfoEntity
import io.reactivex.Completable
import io.reactivex.Single

object CellLocalDataSource {

    private val cellDao by lazy { ThoriumApplication.cellDatabase.cellDao }

    fun insertCellInfo(cellInfoData: CellInfoData): Completable{
        return cellDao.insert(cellInfoData.toCellInfoEntity())
    }

    fun getAll(): Single<List<CellInfoData>>{
        return cellDao.getAll().map {
            it.map { cellInfoEntity ->
                cellInfoEntity.toCellInfoData()
            }
        }
    }

    fun deleteAll(): Completable{
        return cellDao.deleteAll()
    }
}