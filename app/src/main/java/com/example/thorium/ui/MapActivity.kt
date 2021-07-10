package com.example.thorium.ui

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.telephony.*
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import com.example.thorium.Constants
import com.example.thorium.LocationHelper
import com.example.thorium.R
import com.example.thorium.data.entity.CellInfoData
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val networkTypeListener = object : PhoneStateListener() {
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findViewById<Button>(R.id.log_btn).setOnClickListener {
            mapViewModel.getAllInfo()
        }
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

    @SuppressLint("MissingPermission")
    private fun extractServingCellInfo(cellInfo: MutableList<CellInfo>?, location: Location) {
        val servingCellInfo = cellInfo?.first { it.isRegistered }
        var cellInfoData = CellInfoData()
        when (servingCellInfo) {
            // 2G
            is CellInfoGsm -> {
                val identityGsm = servingCellInfo.cellIdentity
                cellInfoData = CellInfoData(
                    identityGsm, location.latitude, location.longitude
                )
            }
            // 3G
            is CellInfoWcdma -> {
                val identityWcdma = servingCellInfo.cellIdentity
                cellInfoData = CellInfoData(
                    identityWcdma, location.latitude, location.longitude
                )
            }
            // 4G
            is CellInfoLte -> {
                val identityLte = servingCellInfo.cellIdentity
                cellInfoData = CellInfoData(
                    identityLte, location.latitude, location.longitude
                )
            }
        }

        cellInfoData = setCellInfoColors(cellInfoData)
        mapViewModel.addCellInfo(cellInfoData)
        drawCircleOnMap(cellInfoData)
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
}