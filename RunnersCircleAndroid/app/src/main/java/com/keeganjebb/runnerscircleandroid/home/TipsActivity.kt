package com.keeganjebb.runnerscircleandroid.home

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import com.keeganjebb.runnerscircleandroid.R
import kotlinx.android.synthetic.main.activity_tips.*

class TipsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tips)

        title = "Tips and Info"

        tipsTextView.movementMethod = ScrollingMovementMethod()

        tipsTextView.text =
                "- Give feedback on Runners' Circle via email by pressing the Feedback option in the top right corner menu\n\n" +
                "- Runners' Circle requires Internet (cell data) to interact with other devices\n\n" +
                "- Use less data and battery during a run by putting phone in sleep mode\n\n" +
                "- Other tracking Apps can be run simultaneously with Runners' Circle\n\n" +
                "- Memory of route, time, distance, and pace will be wiped after starting a new run or exiting the Circle\n\n" +
                "- Run and location data are only shared within the Circle after 'Start' has been pressed\n\n" +
                "- On the map, press any marker to find out more about the runner(s) at that location\n\n" +
                "- The purple map marker is given to the group owner"
    }


}