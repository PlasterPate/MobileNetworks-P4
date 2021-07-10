package com.example.thorium.data.entity

import android.graphics.Color
import android.os.Build
import android.telephony.CellIdentityGsm
import android.telephony.CellIdentityLte
import android.telephony.CellIdentityWcdma
import androidx.room.ColumnInfo

data class CellInfoData(
    val generation: String,
    val code: Int,
    val areaCode: Int,
    val arfcn: Int,
    val mcc: String?,
    val mnc: String?,
    val lat: Double,
    val lng: Double,
    val plmnId: String = mcc + mnc,
    val cellColor: Int = Color.BLACK,
    val acColor: Int = Color.BLACK,
    val plmnColor: Int = Color.BLACK,
){
    constructor(ci: CellIdentityGsm, lat: Double, long: Double) : this(
        "2G",
        ci.bsic,
        ci.lac,
        ci.arfcn,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ci.mccString else ci.mcc.toString(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ci.mncString else ci.mnc.toString(),
        lat,
        long,
    )

    constructor(ci: CellIdentityWcdma, lat: Double, long: Double) : this(
        "3G",
        ci.psc,
        ci.lac,
        ci.uarfcn,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ci.mccString else ci.mcc.toString(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ci.mncString else ci.mnc.toString(),
        lat,
        long,
    )

    constructor(ci: CellIdentityLte, lat: Double, long: Double) : this(
        "4G",
        ci.pci,
        ci.tac,
        ci.earfcn,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ci.mccString else ci.mcc.toString(),
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) ci.mncString else ci.mnc.toString(),
        lat,
        long,
    )

    // Empty constructor for creating temp and dummy objects
    constructor(): this("", 0, 0, 0, "", "", 0.0, 0.0)

}
