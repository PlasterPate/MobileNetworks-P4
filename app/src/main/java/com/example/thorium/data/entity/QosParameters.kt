package com.example.thorium.data.entity

data class QosParameters(
    val uplinkSpeed: Double,
    val downlinkSpeed: Double,
    val cellId: Int,
    val mcc: Int,
    val mnc: Int,
    val strength: Int,
    val rssi: Int? = null,
    val rscp: Int? = null,
    val rsrp: Int? = null,
    val rsrq: Int? = null,
) {
    constructor(
        uplinkSpeed: Double,
        downlinkSpeed: Double
    ) : this(uplinkSpeed, downlinkSpeed, 0, 0, 0, 0)
}
