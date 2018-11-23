package com.keeganjebb.runnerscircleandroid.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.support.StatType
import com.keeganjebb.runnerscircleandroid.support.toDistanceString
import com.keeganjebb.runnerscircleandroid.support.toPaceString
import com.keeganjebb.runnerscircleandroid.support.toTimeString


class StatsRecyclerViewAdapter(aStatMap: Map<StatType, Any>) : RecyclerView.Adapter<StatsRecyclerViewAdapter.ViewHolder>() {

    private val mStatMap = aStatMap

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.layout_stats, p0, false))
    }


    override fun getItemCount(): Int {
        return 2
    }


    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        if (p1 == 0) {

            p0.headingTextView.text = "Personal Stats"

            p0.stat1NameTextView.text = "Time: "
            p0.stat2NameTextView.text = "Distance: "
            p0.stat3NameTextView.text = "Pace: "

            p0.stat1ValueTextView.text = (mStatMap[StatType.TIME] as Int).toTimeString()
            p0.stat2ValueTextView.text = (mStatMap[StatType.DISTANCE] as Double).toDistanceString()
            p0.stat3ValueTextView.text = (mStatMap[StatType.PACE] as Double).toPaceString()

        } else if (p1 == 1) {

            p0.headingTextView.text = "Collective Stats"

            p0.stat1NameTextView.text = "Total Distance: "
            p0.stat2NameTextView.text = "Members Running: "
            p0.stat3NameTextView.text = "Members Connected: "

            p0.stat1ValueTextView.text = (mStatMap[StatType.TOTAL_DISTANCE] as Double).toDistanceString()
            p0.stat2ValueTextView.text = mStatMap[StatType.RUNNING].toString()
            p0.stat3ValueTextView.text = mStatMap[StatType.CONNECTED].toString()
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val headingTextView: TextView = itemView.findViewById(R.id.headingTextView)

        internal val stat1NameTextView: TextView = itemView.findViewById(R.id.stat1NameTextView)
        internal val stat2NameTextView: TextView = itemView.findViewById(R.id.stat2NameTextView)
        internal val stat3NameTextView: TextView = itemView.findViewById(R.id.stat3NameTextView)

        internal val stat1ValueTextView: TextView = itemView.findViewById(R.id.stat1ValueTextView)
        internal val stat2ValueTextView: TextView = itemView.findViewById(R.id.stat2ValueTextView)
        internal val stat3ValueTextView: TextView = itemView.findViewById(R.id.stat3ValueTextView)
    }



}