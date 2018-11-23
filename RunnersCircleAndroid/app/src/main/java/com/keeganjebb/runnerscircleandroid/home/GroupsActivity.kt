package com.keeganjebb.runnerscircleandroid.home

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.keeganjebb.runnerscircleandroid.model.RunGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import android.view.View
import com.keeganjebb.runnerscircleandroid.adapter.GroupsRecyclerViewAdapter
import com.keeganjebb.runnerscircleandroid.adapter.GroupClickedInterface
import com.keeganjebb.runnerscircleandroid.intro.WelcomeActivity
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.run.RunTabActivity
import com.keeganjebb.runnerscircleandroid.support.*
import kotlinx.android.synthetic.main.activity_groups.*


class GroupsActivity : AppCompatActivity(), GroupClickedInterface {

    private val mAuth = FirebaseAuth.getInstance()

    private val userRunGroups = mutableMapOf<String, RunGroup>()

    private var runGroupsDisplayList = arrayListOf<RunGroup>()

    private lateinit var groupsAdapter: GroupsRecyclerViewAdapter

    private lateinit var firebaseListener: ChildEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        title = "My Circles"

        initRecyclerView()

        monitorForSubscribedGroups()
    }


    override fun onStart() {
        super.onStart()

        progressBar.visibility = View.GONE

        goToAddGroupButton.isEnabled = true
        goToSearchGroupButton.isEnabled = true

        reloadRecyclerView() //Re-enable the onClickListener for group cells
    }


    private fun initRecyclerView() {

        val groupsRecyclerView = findViewById<RecyclerView>(R.id.groupsRecyclerView)
        groupsAdapter = GroupsRecyclerViewAdapter(this, runGroupsDisplayList, GroupType.JOINED)
        groupsRecyclerView.adapter = groupsAdapter
        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
    }


    private fun reloadRecyclerView() {

        runGroupsDisplayList.clear()
        runGroupsDisplayList.addAll(ArrayList(userRunGroups.values.sortedBy { RunGroup -> RunGroup.name }))
        groupsAdapter.notifyDataSetChanged()
    }


    private fun monitorForSubscribedGroups() {

        val userId = mAuth.currentUser?.uid

        if (userId != null) {

            val myRef = FirebaseDatabase.getInstance().reference

            val userGroupsDB = myRef.child(FirebaseString.USERS.value).child(userId).child(FirebaseString.GROUPS.value)

            firebaseListener = userGroupsDB.addChildEventListener(object: ChildEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
                override fun onChildRemoved(p0: DataSnapshot) {}

                override fun onChildAdded(userGroupSnapshot: DataSnapshot, p1: String?) {

                    val userGroupKey = userGroupSnapshot.key

                    if (userGroupKey != null) {

                        val runGroupDB = myRef.child(FirebaseString.RUN_GROUPS.value).child(userGroupKey)

                        runGroupDB.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(groupSnapshot: DataSnapshot) {

                                val groupName = groupSnapshot.child(FirebaseString.NAME.value).value as String
                                val groupOwner = groupSnapshot.child(FirebaseString.OWNER.value).value as String
                                val groupID = groupSnapshot.key as String

                                val newRunGroup = RunGroup()
                                newRunGroup.name = groupName
                                newRunGroup.ownerID = groupOwner
                                newRunGroup.fireBaseID = groupID
                                newRunGroup.runsWithGroup = (userGroupSnapshot.value as Long).toInt()

                                val ownerNameDB = myRef.child(FirebaseString.USERS.value).child(groupOwner).child(FirebaseString.NAME.value)

                                ownerNameDB.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {}

                                    override fun onDataChange(ownerNameSnapshot: DataSnapshot) {

                                        newRunGroup.ownerName = ownerNameSnapshot.value as String
                                        userRunGroups[groupID] = newRunGroup

                                        reloadRecyclerView()
                                    }
                                })
                            }
                        })
                    }
                }

                override fun onChildChanged(userGroupSnapshot: DataSnapshot, p1: String?) {

                    val groupID = userGroupSnapshot.key

                    val groupRuns = (userGroupSnapshot.value as Long).toInt()

                    userRunGroups[groupID]?.runsWithGroup = groupRuns

                    reloadRecyclerView()
                }
            })
        }
    }


    override fun groupClicked(group: RunGroup, viewHolder: GroupsRecyclerViewAdapter.ViewHolder) {
        enterGroup(group)
    }


    private fun enterGroup(group: RunGroup) {

        progressBar.visibility = View.VISIBLE

        val userId = mAuth.currentUser?.uid

        if (userId != null) {

            val myRef = FirebaseDatabase.getInstance().reference

            val membersDB = myRef.child(FirebaseString.MEMBERS.value).child(group.fireBaseID).child(userId).child(FirebaseString.ACTIVE.value)

            membersDB.onDisconnect().removeValue()

            membersDB.setValue(true) {error, _ ->

                if (error == null) {

                    runGroupManager = RunGroupManager(group)

                    val runGroupIntent = Intent(this, RunTabActivity::class.java)
                    startActivity(runGroupIntent)
                }
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val inflater = menuInflater
        inflater.inflate(R.menu.groups_menu, menu)

        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when {

            item?.itemId == R.id.profile -> {

                val profileIntent = Intent(applicationContext, ProfileActivity::class.java)
                startActivity(profileIntent)
            }

            item?.itemId == R.id.feedback -> {

                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto:runnerscircleapp@gmail.com")
                intent.putExtra(Intent.EXTRA_SUBJECT,"Runners' Circle Feedback")

                try {
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            item?.itemId == R.id.tips_info -> {

                val tipsIntent = Intent(applicationContext, TipsActivity::class.java)
                startActivity(tipsIntent)

            }

            item?.itemId == R.id.privacy_policy -> {
                openPrivacyPolicy()
            }

            item?.itemId == R.id.logout -> logoutUser()
        }

        return super.onOptionsItemSelected(item)
    }


    private fun openPrivacyPolicy() {

        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://kjebb8.github.io/Runners-Circle/"))
        startActivity(browserIntent)
    }


    fun goToAddGroup(view: View) {

        goToAddGroupButton.isEnabled = false
        val addGroupIntent = Intent(applicationContext, AddGroupActivity::class.java)
        startActivity(addGroupIntent)
    }


    fun goToSearchGroup(view: View) {

        goToSearchGroupButton.isEnabled = false
        val searchGroupIntent = Intent(applicationContext, SearchGroupActivity::class.java)

        val groupIds = ArrayList<String>()

        for (group: RunGroup in runGroupsDisplayList) {
            groupIds.add(group.fireBaseID)
        }

        searchGroupIntent.putExtra("groupIds", groupIds)

        startActivity(searchGroupIntent)
    }


    override fun onBackPressed() {

        AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Log Out?")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Cancel", null)
                .setNegativeButton("Log Out") { _, _ ->
                    logoutUser()
                }
                .show()
    }


    private fun logoutUser() {

        val userID = mAuth.currentUser?.uid

        if (userID != null) {

            val myRef = FirebaseDatabase.getInstance().reference

            val userGroupsDB = myRef.child(FirebaseString.USERS.value).child(userID).child(FirebaseString.GROUPS.value)

            userGroupsDB.removeEventListener(firebaseListener)
        }

        mAuth.signOut()

        val welcomeIntent = Intent(applicationContext, WelcomeActivity::class.java)
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(welcomeIntent)

        finish()
    }



}
