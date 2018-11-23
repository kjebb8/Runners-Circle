package com.keeganjebb.runnerscircleandroid.run

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.keeganjebb.runnerscircleandroid.model.Runner
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.adapter.StatsRecyclerViewAdapter
import com.keeganjebb.runnerscircleandroid.support.RunGroupFragmentInterface
import com.keeganjebb.runnerscircleandroid.support.StatType
import com.keeganjebb.runnerscircleandroid.support.StatsInterface

class StatsFragment : Fragment(), RunGroupFragmentInterface, StatsInterface {

    private val runnersMap = mutableMapOf<String, Runner>()

    private var statsDisplayMap = mutableMapOf(
            StatType.TIME to 0,
            StatType.DISTANCE to 0.0,
            StatType.PACE to 0.0,
            StatType.TOTAL_DISTANCE to 0.0,
            StatType.RUNNING to 0,
            StatType.CONNECTED to 0
    )

    private var statsAdapter: StatsRecyclerViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.stats_fragment, container, false)

        initRecyclerView(view)

        return view
    }


    private fun initRecyclerView(view: View) {

        val statsRecyclerView = view.findViewById<RecyclerView>(R.id.statsRecyclerView)
        statsAdapter = StatsRecyclerViewAdapter(statsDisplayMap)
        statsRecyclerView.adapter = statsAdapter
        statsRecyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext)
    }


    override fun updatedRunners(aRunners: List<Runner>) {
        updateGroupStats(aRunners)
    }


    override fun updateStats(newDistance: Double?, time: Int) {

        statsDisplayMap[StatType.TIME] = time

        if (newDistance != null) {

            statsDisplayMap[StatType.DISTANCE] = newDistance

            if (newDistance > 0) {
                statsDisplayMap[StatType.PACE] = (time.toDouble() / 60) / (newDistance / 1000)
            } else {
                statsDisplayMap[StatType.PACE] = 0.0
            }
        }

        activity?.runOnUiThread {
            statsAdapter?.notifyDataSetChanged()
        }
    }


    private fun updateGroupStats(runnerList: List<Runner>) {

        for (runner: Runner in runnerList) {

            val existingRunner = runnersMap[runner.runnerID]

            if (existingRunner != null) {

                if (existingRunner.connected) {

                    statsDisplayMap[StatType.CONNECTED] = (statsDisplayMap[StatType.CONNECTED] as Int) - 1

                    if (existingRunner.location != null) {
                        statsDisplayMap[StatType.RUNNING] = (statsDisplayMap[StatType.RUNNING] as Int) - 1
                    }
                }

                if (existingRunner.distance != null) {
                    statsDisplayMap[StatType.TOTAL_DISTANCE] = (statsDisplayMap[StatType.TOTAL_DISTANCE] as Double) - existingRunner.distance!!
                }

            } else {

                runnersMap[runner.runnerID] = Runner()
            }

            if (runner.connected) {

                statsDisplayMap[StatType.CONNECTED] = (statsDisplayMap[StatType.CONNECTED] as Int) + 1

                if (runner.location != null) {
                    statsDisplayMap[StatType.RUNNING] = (statsDisplayMap[StatType.RUNNING] as Int) + 1
                }
            }

            if (runner.distance != null) {
                statsDisplayMap[StatType.TOTAL_DISTANCE] = (statsDisplayMap[StatType.TOTAL_DISTANCE] as Double) + runner.distance!!
            }

            runnersMap[runner.runnerID]?.copyRunnerData(runner)
        }

        statsAdapter?.notifyDataSetChanged()
    }



}