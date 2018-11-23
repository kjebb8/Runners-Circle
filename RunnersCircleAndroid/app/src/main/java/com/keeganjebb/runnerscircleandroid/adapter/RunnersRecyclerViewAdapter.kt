package com.keeganjebb.runnerscircleandroid.adapter

import android.graphics.Color
import android.support.v7.widget.CardView
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.model.Runner


class RunnersRecyclerViewAdapter(aRunnerList: ArrayList<Runner>) : RecyclerView.Adapter<RunnersRecyclerViewAdapter.ViewHolder>() {

    private val mRunnerList: ArrayList<Runner> = aRunnerList

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.layout_runner, p0, false))
    }


    override fun getItemCount(): Int {
        return mRunnerList.size + 1
    }


    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        if (p1 == 0) {

            p0.name.setTextColor(Color.BLACK)
            p0.numRuns.setTextColor(Color.BLACK)
            p0.status.setTextColor(Color.BLACK)

            p0.name.text = "Name"
            p0.numRuns.text = "#Runs"
            p0.status.text = "Status"

            p0.backgroundView.setCardBackgroundColor(Color.TRANSPARENT)

        } else {

            val selectedRunner = mRunnerList[p1 - 1]

            p0.name.text = selectedRunner.name
            p0.numRuns.text = selectedRunner.numRunsInGroup.toString()

            if (selectedRunner.location != null) {

                p0.status.text = "Running"
                p0.backgroundView.setCardBackgroundColor(Color.parseColor("#2C2C2C"))

            } else {

                p0.status.text = "Connected"
                p0.backgroundView.setCardBackgroundColor(Color.parseColor("#515151"))
            }
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val name: TextView = itemView.findViewById(R.id.runnerNameTextView)
        internal val numRuns: TextView = itemView.findViewById(R.id.numRunsTextView)
        internal val status: TextView = itemView.findViewById(R.id.statusTextView)
        internal val backgroundView: CardView = itemView.findViewById(R.id.roundedCardView)
    }



}