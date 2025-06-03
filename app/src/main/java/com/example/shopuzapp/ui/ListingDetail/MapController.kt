package com.example.shopuzapp.ui.ListingDetail

import android.content.Context
import org.maplibre.android.annotations.Marker
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView

class MapController(private val context: Context, private val mapView: MapView) {


    fun setupMap(latitude: Double, longitude: Double) {

        mapView.getMapAsync { map ->
            map.setStyle("https://tiles.openfreemap.org/styles/dark")
            map.cameraPosition = CameraPosition.Builder().target(LatLng(latitude, longitude)).zoom(15.0).build()

        }

    }

    fun onStart() {
        mapView.onStart()
    }

    fun onResume() {
        mapView.onResume()
    }

    fun onPause() {
        mapView.onPause()
    }

    fun onStop() {
        mapView.onStop()
    }

    fun onLowMemory() {
        mapView.onLowMemory()
    }

    fun onDestroy() {
        mapView.onDestroy()
    }
}