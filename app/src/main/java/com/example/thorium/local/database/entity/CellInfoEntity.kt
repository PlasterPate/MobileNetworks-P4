package com.example.thorium.local.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cell_info_table")
data class CellInfoEntity(

    @ColumnInfo(name = "generation")
    val generation: String,

    @PrimaryKey
    @ColumnInfo(name = "code")
    val code: Int,

    @ColumnInfo(name = "area_code")
    val areaCode: Int,

    @ColumnInfo(name = "arfcn")
    val arfcn: Int,

    @ColumnInfo(name = "mcc")
    val mcc: String?,

    @ColumnInfo(name = "mnc")
    val mnc: String?,

    @ColumnInfo(name = "plmn_id")
    val plmnId: String = mcc + mnc,
)
