package com.keeganjebb.runnerscircleandroid.model

import com.keeganjebb.runnerscircleandroid.support.toDistanceString
import com.keeganjebb.runnerscircleandroid.support.toPaceString
import com.google.android.gms.maps.model.LatLng

class Runner {

    var runnerID: String = ""
    var name: String = ""
    var numRunsInGroup: Int = 0
    var connected: Boolean = true

    //Optionals are given values when the person is tracking their run
    var location: LatLng? = null
    var distance: Double? = null
    var averagePace: Double? = null

    fun copyRunnerData(templateRunner: Runner) {

        runnerID = templateRunner.runnerID
        name = templateRunner.name
        numRunsInGroup = templateRunner.numRunsInGroup
        connected = templateRunner.connected
        location = templateRunner.location
        distance = templateRunner.distance
        averagePace = templateRunner.averagePace
    }


    fun getMarkerSnippet(): String {

        var distanceString = ""
        if (distance != null) distanceString = distance!!.toDistanceString()

        var paceString = ""
        if (averagePace != null) paceString = averagePace!!.toPaceString()

        return distanceString + "\n" + paceString
    }



}