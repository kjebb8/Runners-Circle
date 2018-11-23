package com.keeganjebb.runnerscircleandroid.run

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.keeganjebb.runnerscircleandroid.model.Runner
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.support.MapInterface
import com.keeganjebb.runnerscircleandroid.support.RunGroupFragmentInterface
import com.keeganjebb.runnerscircleandroid.support.runGroupManager
import com.keeganjebb.runnerscircleandroid.support.toLatLng
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.*
import android.widget.TextView
import android.graphics.Typeface
import android.view.Gravity
import android.widget.LinearLayout
import com.google.android.gms.maps.model.Marker




class MapFragment : Fragment(), RunGroupFragmentInterface, MapInterface {

    private lateinit var mMapView: MapView
    private var mMap: GoogleMap? = null

    private val runnerMarkerMap = mutableMapOf<String, Marker>()

    private var centreLocation: Location? = null

    private var initialMapRegionSet: Boolean = false

    private var ownerID: String? = runGroupManager?.ownerID()
    private var userID: String? = runGroupManager?.userID()

    private val polylineList = mutableListOf<Polyline>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.map_fragment, container, false)

        mMapView = view.findViewById(R.id.mapView)
        mMapView.onCreate(savedInstanceState)

        mMapView.onResume()

        try {
            MapsInitializer.initialize(activity?.applicationContext)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mMapView.getMapAsync { map ->
            mMap = map

            setMarkerInfoWindow()

            showUserLocation()
        }

        return view
    }


    override fun updatedRunners(aRunners: List<Runner>) {
        updateRunnerMarker(aRunners)
    }


    private fun updateRunnerMarker(runnerList: List<Runner>) {

        for (runner: Runner in runnerList) {

            if (runner.runnerID == userID) return

            if (runner.location != null) {

                val marker = runnerMarkerMap[runner.runnerID]

                if (marker != null) {

                    marker.position = runner.location
                    marker.snippet = runner.getMarkerSnippet()

                    if (marker.isInfoWindowShown) { //Reloads the info in the window
                        marker.hideInfoWindow()
                        marker.showInfoWindow()
                    }

                } else { //Need a new marker

                    //Can check out IconGenerator third-party library for putting text on the marker and clusters
                    val newMarkerOptions = MarkerOptions().position(runner.location!!).title(runner.name).snippet(runner.getMarkerSnippet())

                    if (runner.runnerID == ownerID) {
                        newMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    } else {
                        newMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                    }

                    val newMarker = mMap?.addMarker(newMarkerOptions)
                    runnerMarkerMap[runner.runnerID] = newMarker!!
                }

            } else { //No location

                removeRunnerMarker(runner.runnerID)
            }
        }
    }


    private fun removeRunnerMarker(runnerId: String) {

        val marker = runnerMarkerMap[runnerId]

        marker?.remove()
        runnerMarkerMap.remove(runnerId)
    }


    private fun setMarkerInfoWindow() {

        mMap?.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {

            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {

                val context = activity?.applicationContext

                val info = LinearLayout(context)
                info.orientation = LinearLayout.VERTICAL

                val title = TextView(context)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.textSize = 22F
                title.text = marker.title

                val snippet = TextView(context)
                snippet.setTextColor(Color.DKGRAY)
                snippet.textSize = 22F
                snippet.text = marker.snippet

                info.addView(title)
                info.addView(snippet)

                return info
            }
        })
    }


    private fun showUserLocation() {

        if (mMap != null && activity?.applicationContext?.let { ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) } == PackageManager.PERMISSION_GRANTED) {

            mMap?.isMyLocationEnabled = true
            centreMap()
        }
    }


    override fun updateUserOnMap(newLocations: List<Location>) {

        centreLocation = newLocations.last()

        if (mMap != null) {

            if (newLocations.size > 1) {

                val polyline = mMap?.addPolyline(PolylineOptions()
                        .add(newLocations[0].toLatLng(), newLocations[1].toLatLng())
                        .width(12f)
                        .color(Color.BLUE))

                if (polyline != null) {
                    polylineList.add(polyline)
                }
            }

            if (!initialMapRegionSet) {
                showUserLocation() //If permission is given late, need to both centre the map and show location
            }
        }
    }


    override fun clearMap() {

        for (polyline: Polyline in polylineList) {
            polyline.remove()
        }

        polylineList.clear()
    }


    private fun centreMap() {

        if (mMap != null && centreLocation != null) {

            val userLatLng = centreLocation?.toLatLng()

            val cameraPosition = CameraPosition.Builder().target(userLatLng).zoom(15f).build()
            mMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            initialMapRegionSet = true
        }
    }


    override fun onResume() {
        super.onResume()
        mMapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mMapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mMapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mMapView.onLowMemory()
    }



}