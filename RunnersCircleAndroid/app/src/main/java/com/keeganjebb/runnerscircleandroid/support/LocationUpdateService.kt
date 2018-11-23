package com.keeganjebb.runnerscircleandroid.support

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.*
import android.support.v4.app.NotificationCompat
import com.keeganjebb.runnerscircleandroid.run.RunTabActivity
import com.keeganjebb.runnerscircleandroid.R
import android.os.IBinder
import android.app.PendingIntent
import android.app.NotificationManager
import android.app.NotificationChannel
import android.content.pm.PackageManager
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.support.v4.content.ContextCompat


class LocationUpdateService : Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private val CHANNEL_ID = "LocationServiceChannel"

    private lateinit var mLocationManager: LocationManager
    private lateinit var mLocationListener: LocationListener

    override fun onCreate() {
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val serviceChannel = NotificationChannel(CHANNEL_ID,"Location Service Channel", NotificationManager.IMPORTANCE_DEFAULT)

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        setupLocationServices()

        val notificationIntent = Intent(this, RunTabActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Runners' Circle Location Service")
                .setContentText("Your Location is currently being used by Runners' Circle")
                .setSmallIcon(R.drawable.icon_notification)
                .setContentIntent(pendingIntent)
                .build()

        startForeground(1, notification)

        return Service.START_NOT_STICKY
    }


    override fun onDestroy() {
        super.onDestroy()

        stopLocationUpdates()
    }


    private fun setupLocationServices() {

        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mLocationListener = object : LocationListener {

            override fun onLocationChanged(location: Location?) {

                if (location != null) { //&& location.accuracy < 20) {
                    userRunManager?.handleNewLocation(location)
                }
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

            override fun onProviderEnabled(provider: String?) {}

            override fun onProviderDisabled(provider: String?) {}
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) { //Check should always pass since it is required to make the RunGroupManager object

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 15f, mLocationListener)

            val lastKnownLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            if (lastKnownLocation != null) {
                userRunManager?.handleNewLocation(lastKnownLocation)
            }
        }
    }


    private fun stopLocationUpdates() { //Only called when exiting the group. Need to make sure GPS is off to save power
        mLocationManager.removeUpdates(mLocationListener)
    }

}