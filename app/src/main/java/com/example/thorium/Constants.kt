package com.example.thorium

import android.graphics.Color
import com.google.android.gms.maps.model.LatLng

object Constants {

    const val CAMERA_INITIAL_ZOOM = 15f

    const val CIRCLE_RADIUS = 20.0

    const val CIRCLE_STROKE_WIDTH = 0.0F

    const val LOCATION_UPDATE_FASTEST_INTERVAL = 1000L // milliseconds

    const val LOCATION_UPDATE_SMALLEST_DISPLACEMENT = 10.0f // meters

    val INITIAL_LOCATION = LatLng(35.6892, 51.3890) // tehran

    val INITIAL_LOCATION2 = LatLng(32.6581, 51.7084) // isfahan

    const val COLOR_2G = Color.BLUE

    const val COLOR_3G = Color.GREEN

    const val COLOR_4G = Color.RED

    val COLOR_POOL = listOf(
        Color.BLUE,
        Color.BLACK,
        Color.CYAN,
        Color.GREEN,
        Color.RED,
        Color.MAGENTA,
        Color.YELLOW,
        Color.rgb(145, 130, 180),   // Purple
        Color.rgb(128, 0, 0),       // Maroon
        Color.rgb(255, 140, 0),     // Orange
        Color.rgb(140, 70, 20),     // Brown
        Color.rgb(210, 245, 0),     // Lime
        Color.rgb(0, 0, 128),       // Navy
        Color.rgb(255, 105, 180),   // Pink
        Color.rgb(128, 128, 0),     // Olive
    )
}