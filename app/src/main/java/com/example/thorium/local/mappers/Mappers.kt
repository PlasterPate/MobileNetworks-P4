package com.example.thorium.local.mappers

import com.example.thorium.data.entity.CellInfoData
import com.example.thorium.local.database.entity.CellInfoEntity
import com.example.thorium.local.database.entity.LocationEntity

fun CellInfoData.toCellInfoEntity(): CellInfoEntity {
    return CellInfoEntity(generation, code, areaCode, arfcn, mcc, mnc, plmnId)
}


fun LocationEntity.toCellInfoData(): CellInfoData{
    return CellInfoData(
        cellInfoEntity.generation,
        cellInfoEntity.code,
        cellInfoEntity.areaCode,
        cellInfoEntity.arfcn,
        cellInfoEntity.mcc,
        cellInfoEntity.mnc,
        lat,
        lng,
        cellInfoEntity.plmnId
    )
}

fun CellInfoData.toLocationEntity(): LocationEntity{
    return LocationEntity(
        0,
        lat,
        lng,
        CellInfoEntity(generation, code, areaCode, arfcn, mcc, mnc, plmnId)
    )
}

fun CellInfoEntity.toCellInfoData(): CellInfoData{
    return CellInfoData(
        generation, code, areaCode, arfcn, mcc, mnc, 0.0, 0.0, plmnId
    )
}