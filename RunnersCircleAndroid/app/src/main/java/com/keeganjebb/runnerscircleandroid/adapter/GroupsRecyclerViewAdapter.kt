package com.keeganjebb.runnerscircleandroid.adapter

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.keeganjebb.runnerscircleandroid.model.RunGroup
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.support.GroupType


interface GroupClickedInterface {
    fun groupClicked(group: RunGroup, viewHolder: GroupsRecyclerViewAdapter.ViewHolder)
}


class GroupsRecyclerViewAdapter(aListener: GroupClickedInterface, aRunGroups: ArrayList<RunGroup>, aGroupType: GroupType) : RecyclerView.Adapter<GroupsRecyclerViewAdapter.ViewHolder>() {

    private val mRunGroups: ArrayList<RunGroup> = aRunGroups
    private val mGroupClickListener: GroupClickedInterface = aListener
    private val mGroupType: GroupType = aGroupType

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {

        return when (mGroupType == GroupType.JOINED) {

            true -> ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.layout_run_group, p0, false))
            false -> ViewHolder(LayoutInflater.from(p0.context).inflate(R.layout.layout_search_group, p0, false))
        }
    }


    override fun getItemCount(): Int {
        return mRunGroups.size
    }


    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

        val selectedGroup = mRunGroups[p1]

        p0.groupName.text = selectedGroup.name
        p0.ownerName.text = "Owner: " + selectedGroup.ownerName
        p0.background.isEnabled = true

        if (mGroupType == GroupType.JOINED) {
            p0.runsInGroup?.text = "Runs With Circle: ${selectedGroup.runsWithGroup}"
        }

        p0.background.setOnClickListener {

            p0.background.isEnabled = false
            mGroupClickListener.groupClicked(selectedGroup, p0)
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        internal val groupName: TextView
        internal var runsInGroup: TextView? = null
        internal val ownerName: TextView
        internal val background: ConstraintLayout

        init {

            if (mGroupType == GroupType.JOINED) {

                groupName = itemView.findViewById(R.id.groupNameTextView)
                ownerName = itemView.findViewById(R.id.ownerTextView)
                background = itemView.findViewById(R.id.mainBackground)
                runsInGroup = itemView.findViewById(R.id.runsWithGroupTextView)

            } else {

                groupName = itemView.findViewById(R.id.searchGroupNameTextView)
                ownerName = itemView.findViewById(R.id.searchOwnerTextView)
                background = itemView.findViewById(R.id.searchMainBackground)
            }
        }
    }



}