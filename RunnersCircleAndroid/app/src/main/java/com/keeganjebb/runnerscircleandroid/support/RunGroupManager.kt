package com.keeganjebb.runnerscircleandroid.support

import com.keeganjebb.runnerscircleandroid.model.RunGroup
import com.keeganjebb.runnerscircleandroid.model.Runner
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


interface RunGroupFragmentInterface {
    fun updatedRunners(aRunners: List<Runner>)
}

interface RunGroupActivityInterface {
    fun notifyDatabaseDisconnect()
}

var runGroupManager: RunGroupManager? = null //Only 1 global instance

class RunGroupManager (aRunGroup: RunGroup) {

    private val mAuth = FirebaseAuth.getInstance()

    private lateinit var firebaseListener: ChildEventListener

    private val mRunGroup = aRunGroup

    private val mRunnersMap = mutableMapOf<String, Runner>()

    private var mRunGroupFragmentListener: RunGroupFragmentInterface? = null

    private var mRunGroupActivityListener: RunGroupActivityInterface? = null

    var databaseReadPeriod: Int = 4 //Number of seconds between database reads

    fun ownerID(): String {
        return mRunGroup.ownerID
    }


    fun userID(): String? {
        return mAuth.currentUser?.uid
    }


    fun runGroupName(): String {
        return mRunGroup.name
    }


    fun setFragmentListenerAs(newListener: RunGroupFragmentInterface?) {

        mRunGroupFragmentListener = newListener
        mRunGroupFragmentListener?.updatedRunners(mRunnersMap.values.toList())
    }


    fun setActivityListenerAs(newListener: RunGroupActivityInterface?) {
        mRunGroupActivityListener = newListener
    }


    fun loadActiveRunners() {

        val groupId = mRunGroup.fireBaseID

        val myRef = FirebaseDatabase.getInstance().reference

        val membersDB = myRef.child(FirebaseString.MEMBERS.value).child(groupId)

        membersDB.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(membersSnapshot: DataSnapshot) {

                for (member: DataSnapshot in membersSnapshot.children) {

                    val memberId = member.key as String

                    val lat = member.child(FirebaseString.LATITUDE.value).value as? Double
                    val long = member.child(FirebaseString.LONGITUDE.value).value as? Double
                    val dist = member.child(FirebaseString.DISTANCE.value).value as? Double
                    val pace = member.child(FirebaseString.AVG_PACE.value).value as? Double

                    var latLng: LatLng? = null

                    if (lat != null && long != null) {
                        latLng = LatLng(lat, long)
                    }

                    val runner = mRunnersMap[memberId]

                    if (runner != null) { //If already active runner, update their object with new data

                        if ((runner.location != null && latLng == null) || !runner.connected) {

                            runner.connected = true

                            val userGroupsDB = myRef.child(FirebaseString.USERS.value).child(memberId).child(FirebaseString.GROUPS.value).child(groupId)

                            userGroupsDB.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(userGroupSnapshot: DataSnapshot) {

                                    val groupRuns = (userGroupSnapshot.value as Long).toInt()
                                    runner.numRunsInGroup = groupRuns
                                }
                            })
                        }

                        runner.location = latLng

                        if (runner.location != null) {

                            runner.distance = dist
                            runner.averagePace = pace
                        }

                        mRunGroupFragmentListener?.updatedRunners(listOf(runner))

                    } else { //If not already an active runner, add them to the active list

                        val userDB = myRef.child(FirebaseString.USERS.value).child(memberId)

                        userDB.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(userSnapshot: DataSnapshot) {

                                val userName = userSnapshot.child(FirebaseString.NAME.value).value as String

                                val groupRuns = (userSnapshot.child(FirebaseString.GROUPS.value).child(groupId).value as Long).toInt()

                                val newRunner = Runner()
                                newRunner.runnerID = memberId
                                newRunner.name = userName
                                newRunner.numRunsInGroup = groupRuns
                                newRunner.location = latLng
                                newRunner.distance = dist
                                newRunner.averagePace = pace

                                mRunnersMap[memberId] = newRunner

                                mRunGroupFragmentListener?.updatedRunners(listOf(newRunner))
                            }
                        })
                    }
                }
            }
        })
    }


    fun monitorForRemovedRunners() {

        val groupId = mRunGroup.fireBaseID

        val myRef = FirebaseDatabase.getInstance().reference

        val membersDB = myRef.child(FirebaseString.MEMBERS.value).child(groupId)

        firebaseListener = membersDB.addChildEventListener(object : ChildEventListener {
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {}

            override fun onChildRemoved(memberRemovedSnapshot: DataSnapshot) {

                val memberId = memberRemovedSnapshot.key as String

                if (memberId == userID()) {

                    mRunGroupActivityListener?.notifyDatabaseDisconnect()

                } else {

                    mRunnersMap[memberId]?.let {removedRunner ->

                        removedRunner.location = null
                        removedRunner.connected = false

                        mRunGroupFragmentListener?.updatedRunners(listOf(removedRunner))
                    }
                }
            }
        })
    }



    fun uploadUserRunData(latLng: LatLng, distance: Double, pace: Double?) {

        val uid = userID()

        if (uid != null) {

            val groupId = mRunGroup.fireBaseID

            val myRef = FirebaseDatabase.getInstance().reference

            val membersDB = myRef.child(FirebaseString.MEMBERS.value).child(groupId).child(uid)

            val runDataMap = mapOf(
                    FirebaseString.ACTIVE.value to true,
                    FirebaseString.LATITUDE.value to latLng.latitude,
                    FirebaseString.LONGITUDE.value to latLng.longitude,
                    FirebaseString.DISTANCE.value to distance,
                    FirebaseString.AVG_PACE.value to pace)

            membersDB.onDisconnect().removeValue()
            membersDB.updateChildren(runDataMap)
        }
    }


    fun removeRunningData() {

        val uid = userID()

        if (uid != null) {

            val groupId = mRunGroup.fireBaseID

            val myRef = FirebaseDatabase.getInstance().reference

            val membersDB = myRef.child(FirebaseString.MEMBERS.value).child(groupId).child(uid)

            val runDataMap = mapOf<String, Any?>(
                    FirebaseString.LATITUDE.value to null,
                    FirebaseString.LONGITUDE.value to null,
                    FirebaseString.DISTANCE.value to null,
                    FirebaseString.AVG_PACE.value to null)

            membersDB.updateChildren(runDataMap)
        }
    }


    fun incrementRunsInGroup() {

        val uid = userID()

        if (uid != null) {

            mRunnersMap[uid]?.let {user ->

                user.numRunsInGroup++

                val groupId = mRunGroup.fireBaseID

                val myRef = FirebaseDatabase.getInstance().reference

                val userGroupDB = myRef.child(FirebaseString.USERS.value).child(uid).child(FirebaseString.GROUPS.value).child(groupId)

                userGroupDB.setValue(user.numRunsInGroup)
            }
        }
    }


    fun removeRunnerFromGroup() {

        val uid = userID()

        if (uid != null) {

            val groupId = mRunGroup.fireBaseID

            val myRef = FirebaseDatabase.getInstance().reference

            val membersDB = myRef.child(FirebaseString.MEMBERS.value).child(groupId)

            membersDB.removeEventListener(firebaseListener)

            membersDB.child(uid).removeValue()
        }
    }


    fun addUserBackToGroup() {

        val uid = userID()

        if (uid != null) {

            val groupId = mRunGroup.fireBaseID

            val myRef = FirebaseDatabase.getInstance().reference

            val membersDB = myRef.child(FirebaseString.MEMBERS.value).child(groupId).child(uid).child(FirebaseString.ACTIVE.value)

            membersDB.onDisconnect().removeValue()
            membersDB.setValue(true)

            loadActiveRunners()
        }
    }


}