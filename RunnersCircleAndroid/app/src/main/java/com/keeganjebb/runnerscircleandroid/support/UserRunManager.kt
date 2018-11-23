package com.keeganjebb.runnerscircleandroid.support

import android.location.Location
import java.io.Serializable


interface MapInterface : Serializable {

    fun updateUserOnMap(newLocations: List<Location>)
    fun clearMap()
}


interface StatsInterface : Serializable {
    fun updateStats(newDistance: Double?, time: Int)
}

var userRunManager: UserRunManager? = null //Only 1 global instance

class UserRunManager (aMapListener: MapInterface, aStatsListener: StatsInterface) : Serializable {

    private var mMapListener = aMapListener
    private var mStatsListener = aStatsListener

    var runStarted: Boolean = false
    private var mRunTime: Int = 0 //Seconds, Incremented through RunTabActivity

    private var mDistance: Double = 0.0 //Meters
    private var mLocationList = mutableListOf<Location>()
    private var currentLocation: Location? = null

    fun startRun() {

        runStarted = true

        mRunTime = 0
        mDistance = 0.0
        mLocationList.clear()

        mMapListener.clearMap()
        mStatsListener.updateStats(mDistance, mRunTime)

        if (currentLocation != null) {

            runGroupManager?.uploadUserRunData(currentLocation!!.toLatLng(), mDistance, null)
            mLocationList.add(currentLocation!!)
        }
    }


    fun incrementRunTime() {

        mRunTime++
        mStatsListener.updateStats(null, mRunTime)
    }


    fun endRun() {

        runStarted = false

        if (mDistance > 1000) {
            runGroupManager?.incrementRunsInGroup()
        }

        runGroupManager?.removeRunningData()
    }


    fun handleNewLocation(newLocation: Location) {

        if (runStarted) {

            if (mLocationList.isNotEmpty()) {

                val lastLocation = mLocationList.last()

                val delta = newLocation.distanceTo(lastLocation).toDouble()
                mDistance += delta

                mStatsListener.updateStats(mDistance, mRunTime)
                mMapListener.updateUserOnMap(listOf(lastLocation, newLocation))
            }

            mLocationList.add(newLocation)

            var newPace: Double? = null

            if (mDistance != 0.0) {
                newPace = mRunTime.toDouble() / mDistance * 1000 / 60
            }

            runGroupManager?.uploadUserRunData(newLocation.toLatLng(), mDistance, newPace)

        } else {

            mMapListener.updateUserOnMap(listOf(newLocation))
        }

        currentLocation = newLocation
    }


}