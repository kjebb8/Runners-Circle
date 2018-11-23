package com.keeganjebb.runnerscircleandroid.run

import android.content.*
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.support.*
import kotlinx.android.synthetic.main.activity_run_tab.*
import android.support.v4.view.ViewPager.OnPageChangeListener
import android.support.v7.app.AlertDialog
import android.view.Menu
import android.view.MenuItem
import com.keeganjebb.runnerscircleandroid.adapter.SectionsPageAdapter
import java.util.*


class RunTabActivity : AppCompatActivity(), RunGroupActivityInterface {

    private val fragmentList = listOf(LobbyFragment(), MapFragment(), StatsFragment())

    private val timer = Timer()
    private var timeCheck: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_tab)

        toolbar.title = runGroupManager?.runGroupName()

        val toolbar = findViewById<android.support.v7.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val viewPager = findViewById<ViewPager>(R.id.container)
        setupViewPager(viewPager)

        val tabLayout = findViewById<TabLayout>(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)

        runGroupManager?.setFragmentListenerAs(fragmentList[0] as RunGroupFragmentInterface)
        runGroupManager?.setActivityListenerAs(this)
        setupRunTimer()
        runGroupManager?.loadActiveRunners()
        runGroupManager?.monitorForRemovedRunners()

        askForLocationPermissions()
    }


    private fun setupViewPager(viewPager: ViewPager) {

        val adapter = SectionsPageAdapter(supportFragmentManager)

        adapter.addFragment(fragmentList[0] as Fragment, "Lobby")
        adapter.addFragment(fragmentList[1] as Fragment, "Map")
        adapter.addFragment(fragmentList[2] as Fragment, "Stats")

        viewPager.adapter = adapter

        viewPager.addOnPageChangeListener(object: OnPageChangeListener{
            override fun onPageScrollStateChanged(p0: Int) {}
            override fun onPageScrolled(p0: Int, p1: Float, p2: Int) {}

            override fun onPageSelected(p0: Int) {
                runGroupManager?.setFragmentListenerAs(fragmentList[p0] as RunGroupFragmentInterface)
            }
        })
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.menu_run_tab, menu)

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        if (item?.itemId == R.id.startStopTool) {

            if (userRunManager != null) {

                if (!userRunManager!!.runStarted) {

                    userRunManager?.startRun()
                    item.title = "Stop"

                } else {

                    userRunManager?.endRun()
                    item.title = "Start"
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }


    private fun askForLocationPermissions() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setupUserRun()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 1) {

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupUserRun()
            }
        }
    }


    private fun setupUserRun() {

        userRunManager = UserRunManager(fragmentList[1] as MapInterface, fragmentList[2] as StatsInterface)
        startLocationUpdateService()
    }


    private fun setupRunTimer() {

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {

                timeCheck++

                if (timeCheck >= (runGroupManager?.databaseReadPeriod)!!) { //>= so that it enters if the period is changed from 60 to 4 when entering foreground

                    runGroupManager?.loadActiveRunners()
                    timeCheck = 0
                }

                if (userRunManager != null && userRunManager!!.runStarted) userRunManager?.incrementRunTime()
            }
        }, 1000, 1000)
    }


    override fun notifyDatabaseDisconnect() {

        AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Disconnected from Server")
                .setPositiveButton("Stay in Circle") {_, _ ->
                    runGroupManager?.addUserBackToGroup()
                }
                .setNegativeButton("Exit Circle") {_, _ ->
                    exitGroup()
                }
                .show()
    }


    override fun onBackPressed() {

        if (userRunManager != null) {

            if (!userRunManager!!.runStarted) {

            exitGroup()

            } else {

                AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Exit Circle?")
                        .setMessage("Are you sure you want to exit while running?")
                        .setPositiveButton("Stay in Circle", null)
                        .setNegativeButton("Exit Circle") { _, _ ->
                            exitGroup()
                        }
                        .show()
            }

        } else {

            exitGroup()
        }
    }


    private fun exitGroup() {

        timer.cancel()
        timer.purge()

        stopLocationUpdateService()
        if (userRunManager != null && userRunManager!!.runStarted) userRunManager?.endRun()

        runGroupManager?.removeRunnerFromGroup()

        runGroupManager = null
        userRunManager = null

        finish()
    }


    override fun onStop() {
        super.onStop()

        runGroupManager?.databaseReadPeriod = 55
    }


    override fun onRestart() {
        super.onRestart()

        runGroupManager?.databaseReadPeriod = 4
    }


    //Location Service Content

    //----------------------------------------------

    override fun onDestroy() {
        super.onDestroy()

        stopLocationUpdateService()
    }


    private fun startLocationUpdateService() {

        if (userRunManager != null) {

            val startServiceIntent = Intent(this, LocationUpdateService::class.java)
            ContextCompat.startForegroundService(this, startServiceIntent)
        }
    }


    private fun stopLocationUpdateService() {

        val stopServiceIntent = Intent(this, LocationUpdateService::class.java)
        stopService(stopServiceIntent)
    }




}
