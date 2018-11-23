package com.keeganjebb.runnerscircleandroid.support

import android.content.Context
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt



fun Location.toLatLng(): LatLng {
    return LatLng(this.latitude, this.longitude)
}


fun Int.toTimeString(): String {

    val hours: Int =  this / 3600
    val minutes: Int = this / 60 % 60
    val seconds: Int = this % 60

    return if (hours >= 1) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}


fun Double.toDistanceString(): String {

    val km = ((this / 10).roundToInt().toDouble() / 100)

    return String.format("%.2f", km) + " km"
}


fun Double.toPaceString(): String {

    val minutes: Int = Math.floor(this).toInt()
    val seconds: Int = ((this - minutes) * 60).roundToInt()

    return String.format("%d:%02d", minutes, seconds) + " min/km"
}


fun AppCompatActivity.hideSoftKeyboard() {

    val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(currentFocus?.windowToken, InputMethodManager.SHOW_FORCED)
}


//Was showing a keyboard that I couldn't remove when clicking background (after pressing the active button and getting an alert)

//fun AppCompatActivity.showSoftKeyboard() {
//
//    val inputManager: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
//}

