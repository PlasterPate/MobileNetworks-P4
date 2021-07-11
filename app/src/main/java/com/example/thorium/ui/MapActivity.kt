package com.example.thorium.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telephony.*
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.example.thorium.Constants
import com.example.thorium.LocationHelper
import com.example.thorium.R
import com.example.thorium.data.entity.CellInfoData
import com.example.thorium.data.entity.QosParameters
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.ktx.addCircle


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = "MapsActivity"
    private val PERMISSIONS_REQUEST_LOCATION = 123

    private var mMap: GoogleMap? = null
    private var menuItemId = R.id.option_tech

    private val acColorMap = mutableMapOf<Int, Int>()
    private val cellColorMap = mutableMapOf<Int, Int>()
    private val plmnColorMap = mutableMapOf<String, Int>()


    private val locationHelper by lazy {
        LocationHelper(this, mMap)
    }

    private val mapViewModel by lazy {
        ViewModelProvider(this)[MapViewModel::class.java]
    }

    private lateinit var telephonyManager: TelephonyManager

    private lateinit var connectivityManager: ConnectivityManager

    private var networkCapabilities: NetworkCapabilities? = null

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val networkTypeListener = object : PhoneStateListener() {
        @RequiresApi(Build.VERSION_CODES.O)
        @SuppressLint("MissingPermission")
        override fun onDataConnectionStateChanged(state: Int, networkType: Int) {
            if (networkType != 0) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    extractServingCellInfo(telephonyManager.allCellInfo, location)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initializeObservers()

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        networkCapabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

//        findViewById<Button>(R.id.log_btn).setOnClickListener {
//            mapViewModel.getAllInfo()
//        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        locationHelper.getLocationPermission(PERMISSIONS_REQUEST_LOCATION)

        val cameraPosition = CameraPosition.Builder()
            .target(Constants.INITIAL_LOCATION)
            .zoom(Constants.CAMERA_INITIAL_ZOOM)
            .build()
        mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

        if (locationHelper.locationPermissionGranted)
            getTelephonyInfo()

    }

    @SuppressLint("MissingPermission")
    private fun getTelephonyInfo() {
        val locationRequest = LocationRequest.create()
            .setFastestInterval(Constants.LOCATION_UPDATE_FASTEST_INTERVAL)
            .setSmallestDisplacement(Constants.LOCATION_UPDATE_SMALLEST_DISPLACEMENT)

        val locationCallback = object : LocationCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.locations.forEach {
                    extractServingCellInfo(
                        (getSystemService(TELEPHONY_SERVICE) as TelephonyManager).allCellInfo,
                        it
                    )
                }
            }
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        telephonyManager.listen(
            networkTypeListener,
            PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingPermission")
    private fun extractServingCellInfo(cellInfo: MutableList<CellInfo>?, location: Location) {
        val servingCellInfo = cellInfo?.first { it.isRegistered }
        var cellInfoData = CellInfoData()
        var qosParameters = QosParameters(
            networkCapabilities!!.linkUpstreamBandwidthKbps / 1000.0,
            networkCapabilities!!.linkDownstreamBandwidthKbps / 1000.0,
        )
        when (servingCellInfo) {
            // 2G
            is CellInfoGsm -> {
                val identityGsm = servingCellInfo.cellIdentity
                cellInfoData = CellInfoData(
                    identityGsm, location.latitude, location.longitude
                )
                with(servingCellInfo.cellSignalStrength) {
                    qosParameters = qosParameters.copy(
                        cellId = identityGsm.cid,
                        mcc = cellInfoData.mcc?.toInt() ?: 0,
                        mnc = cellInfoData.mnc?.toInt() ?: 0,
                        strength = dbm,
                        rssi = asuLevel
                    )
                }
            }
            // 3G
            is CellInfoWcdma -> {
                val identityWcdma = servingCellInfo.cellIdentity
                cellInfoData = CellInfoData(
                    identityWcdma, location.latitude, location.longitude
                )
                Log.d(TAG, "3G")
                with(servingCellInfo.cellSignalStrength) {
                    qosParameters = qosParameters.copy(
                        cellId = identityWcdma.cid,
                        mcc = cellInfoData.mcc?.toInt() ?: 0,
                        mnc = cellInfoData.mnc?.toInt() ?: 0,
                        strength = dbm,
                        rscp = asuLevel
                    )
                }
            }
            // 4G
            is CellInfoLte -> {
                val identityLte = servingCellInfo.cellIdentity
                cellInfoData = CellInfoData(
                    identityLte, location.latitude, location.longitude
                )
                Log.d(TAG, "4G")
                with(servingCellInfo.cellSignalStrength) {
                    qosParameters = qosParameters.copy(
                        cellId = identityLte.ci,
                        mcc = cellInfoData.mcc?.toInt() ?: 0,
                        mnc = cellInfoData.mnc?.toInt() ?: 0,
                        strength = dbm,
                        rscp = this.asuLevel,
                        rsrp = rsrp,
                        rsrq = rsrq
                    )
                }
            }
        }

        cellInfoData = setCellInfoColors(cellInfoData)
        mapViewModel.addCellInfo(cellInfoData)
        drawCircleOnMap(cellInfoData)
        writeQosParameters(qosParameters)
    }

    private fun setCellInfoColors(cellInfoData: CellInfoData): CellInfoData {
        var newCellInfoData = cellInfoData.copy()

        // Setting area code color
        if (cellInfoData.areaCode in acColorMap.keys)
            newCellInfoData = newCellInfoData.copy(acColor = acColorMap[cellInfoData.areaCode]!!)
        else {
            pickNewColor(acColorMap.values.toList()).also { newColor ->
                acColorMap[cellInfoData.areaCode] = newColor
                newCellInfoData = newCellInfoData.copy(acColor = newColor)
            }
        }

        // Setting cell code color
        if (cellInfoData.code in cellColorMap.keys)
            newCellInfoData = newCellInfoData.copy(cellColor = cellColorMap[cellInfoData.code]!!)
        else {
            pickNewColor(cellColorMap.values.toList()).also { newColor ->
                cellColorMap[cellInfoData.code] = newColor
                newCellInfoData = newCellInfoData.copy(cellColor = newColor)
            }
        }

        // Setting PLMN color
        if (cellInfoData.plmnId in plmnColorMap.keys)
            newCellInfoData = newCellInfoData.copy(plmnColor = plmnColorMap[cellInfoData.plmnId]!!)
        else {
            pickNewColor(plmnColorMap.values.toList()).also { newColor ->
                plmnColorMap[cellInfoData.plmnId] = newColor
                newCellInfoData = newCellInfoData.copy(plmnColor = newColor)
            }
        }

        return newCellInfoData
    }

    private fun drawCircleOnMap(cellInfoData: CellInfoData) {
        when (menuItemId) {
            R.id.option_tech -> {
                colorByTechnology(cellInfoData)
            }
            R.id.option_ac -> {
                colorByAreaCode(cellInfoData)
            }
            R.id.option_cell -> {
                colorByCellCode(cellInfoData)
            }
            R.id.option_plmn -> {
                colorByPlmn(cellInfoData)
            }
        }
    }

    private fun colorByTechnology(cellInfoData: CellInfoData) {
        val color = when (cellInfoData.generation) {
            "2G" -> Constants.COLOR_2G
            "3G" -> Constants.COLOR_3G
            "4G" -> Constants.COLOR_4G
            else -> Color.GRAY
        }
        drawCircleWithColor(LatLng(cellInfoData.lat, cellInfoData.lng), color)
    }

    private fun colorByAreaCode(cellInfoData: CellInfoData) {
        drawCircleWithColor(LatLng(cellInfoData.lat, cellInfoData.lng), cellInfoData.acColor)
    }

    private fun colorByCellCode(cellInfoData: CellInfoData) {
        drawCircleWithColor(LatLng(cellInfoData.lat, cellInfoData.lng), cellInfoData.cellColor)
    }

    private fun colorByPlmn(cellInfoData: CellInfoData) {
        drawCircleWithColor(LatLng(cellInfoData.lat, cellInfoData.lng), cellInfoData.plmnColor)
    }

    private fun drawCircleWithColor(center: LatLng, color: Int) {
        mMap?.addCircle {
            center(center)
            radius(Constants.CIRCLE_RADIUS)
            fillColor(color)
            strokeWidth(Constants.CIRCLE_STROKE_WIDTH)
        }
    }

    private fun pickNewColor(codeMapValues: List<Int>): Int {
        var newColor = Constants.COLOR_POOL.random()
        while (newColor in codeMapValues) {
            newColor = Constants.COLOR_POOL.random()
        }
        return newColor
    }

    private fun initializeObservers() {
        mapViewModel.allLocations.observe(this, {
            mMap?.clear()
            it.forEach { cellInfoData ->
                val newCellInfoData = setCellInfoColors(cellInfoData)
                drawCircleOnMap(newCellInfoData)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        item.isChecked = true
        mapViewModel.getAllInfo()
        menuItemId = item.itemId

        val legend = findViewById<ConstraintLayout>(R.id.tech_legend)
        legend.visibility =
            if (menuItemId == R.id.option_tech)
                View.VISIBLE
            else
                View.GONE

        return true
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.options, menu)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationHelper.getLocationPermission(requestCode)
                    getTelephonyInfo()
                }
            }
        }
    }

    private fun writeQosParameters(qosParameters: QosParameters) {
        val cellIdTxt = findViewById<TextView>(R.id.cell_id_txt)
        cellIdTxt.text = String.format(cellIdTxt.text.toString(), qosParameters.cellId)

        val strengthIdTxt = findViewById<TextView>(R.id.strength_txt)
        strengthIdTxt.text = String.format(strengthIdTxt.text.toString(), qosParameters.strength)

        val uplinkTxt = findViewById<TextView>(R.id.uplink_txt)
        uplinkTxt.text = String.format(uplinkTxt.text.toString(), qosParameters.uplinkSpeed)

        val downlinkTxt = findViewById<TextView>(R.id.downlink_txt)
        downlinkTxt.text = String.format(downlinkTxt.text.toString(), qosParameters.downlinkSpeed)

        val mccTxt = findViewById<TextView>(R.id.mcc_txt)
        mccTxt.text = String.format(mccTxt.text.toString(), qosParameters.mcc)
        val mncTxt = findViewById<TextView>(R.id.mnc_txt)
        mncTxt.text = String.format(mncTxt.text.toString(), qosParameters.mnc)

        val asuTxt = findViewById<TextView>(R.id.asu_txt)
        asuTxt.text = if (qosParameters.rssi != null)
            String.format(asuTxt.text.toString(), "rssi", qosParameters.rssi)
        else
            String.format(asuTxt.text.toString(), "rscp", qosParameters.rscp)

        val rsrpTxt = findViewById<TextView>(R.id.rsrp_txt)
        if (qosParameters.rsrp != null) {
            rsrpTxt.text = String.format(rsrpTxt.text.toString(), qosParameters.rsrp)
            rsrpTxt.visibility = View.VISIBLE
        } else{
            rsrpTxt.visibility = View.GONE
        }

        val rsrqTxt = findViewById<TextView>(R.id.rsrq_txt)
        if (qosParameters.rsrq != null) {
            rsrqTxt.text = String.format(rsrqTxt.text.toString(), qosParameters.rsrq)
            rsrqTxt.visibility = View.VISIBLE
        } else{
            rsrqTxt.visibility = View.GONE
        }
    }
}