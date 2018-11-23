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
import com.keeganjebb.runnerscircleandroid.adapter.RunnersRecyclerViewAdapter
import com.keeganjebb.runnerscircleandroid.support.RunGroupFragmentInterface

class LobbyFragment : Fragment(), RunGroupFragmentInterface {

    private val runnersMap = mutableMapOf<String, Runner>()

    private var runnersDisplayList = arrayListOf<Runner>()

    private var runnersAdapter: RunnersRecyclerViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.lobby_fragment, container, false)

        initRecyclerView(view)

        return view
    }


    private fun initRecyclerView(view: View) {

        val runnersRecyclerView = view.findViewById<RecyclerView>(R.id.lobbyRecyclerView)
        runnersAdapter = RunnersRecyclerViewAdapter(runnersDisplayList)
        runnersRecyclerView.adapter = runnersAdapter
        runnersRecyclerView.layoutManager = LinearLayoutManager(activity?.applicationContext)

        reloadRecyclerView() //If runnersMap is updated via loadRunnerList before the adapter is initialized, then this has to be called after the adapter is initialized for it to show the runners

    }


    private fun reloadRecyclerView() {

        runnersDisplayList.clear()
        val baseRunnerList = runnersMap.values.filter { Runner -> Runner.connected }
        runnersDisplayList.addAll(ArrayList(baseRunnerList.sortedBy { Runner -> Runner.name }))
        runnersAdapter?.notifyDataSetChanged()
    }


    override fun updatedRunners(aRunners: List<Runner>) {

        for (runner: Runner in aRunners) {
            runnersMap[runner.runnerID] = runner
        }

        reloadRecyclerView()
    }



}