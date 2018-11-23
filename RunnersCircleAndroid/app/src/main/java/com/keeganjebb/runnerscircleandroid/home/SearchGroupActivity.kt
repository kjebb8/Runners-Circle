package com.keeganjebb.runnerscircleandroid.home

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.EditText
import android.widget.LinearLayout
import com.keeganjebb.runnerscircleandroid.adapter.GroupClickedInterface
import com.keeganjebb.runnerscircleandroid.support.FirebaseString
import com.keeganjebb.runnerscircleandroid.support.GroupType
import com.keeganjebb.runnerscircleandroid.adapter.GroupsRecyclerViewAdapter
import com.keeganjebb.runnerscircleandroid.model.RunGroup
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.support.hideSoftKeyboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_search_group.*

class SearchGroupActivity : AppCompatActivity(), GroupClickedInterface {

    private val mAuth = FirebaseAuth.getInstance()

    lateinit var userRunGroupIds: ArrayList<String>

    var baseRunGroupsList = arrayListOf<RunGroup>()

    var displayedRunGroups = arrayListOf<RunGroup>()

    private lateinit var searchGroupsAdapter: GroupsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_group)

        title = "Search Circles"

        setupSearchView()

        userRunGroupIds = intent.getStringArrayListExtra("groupIds")

        initRecyclerView()

        downloadGroups()

    }


    private fun setupSearchView() {

        groupSearchView.queryHint = "Search By Circle Name"
        groupSearchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(searchText: String?): Boolean {

                if (searchText != null && searchText.isNotEmpty()) {
                    downloadGroups(searchText)
                }

                hideSoftKeyboard()

                return true
            }

            override fun onQueryTextChange(searchText: String?): Boolean {

                if (searchText != null && searchText.isEmpty()) {
                    displayedRunGroups.clear()
                    displayedRunGroups.addAll(baseRunGroupsList)
                    searchGroupsAdapter.notifyDataSetChanged()
                }
                return true
            }

        })
    }


    private fun initRecyclerView() {

        val groupsRecyclerView = findViewById<RecyclerView>(R.id.searchedGroupsRecyclerView)
        searchGroupsAdapter = GroupsRecyclerViewAdapter(this, displayedRunGroups, GroupType.SEARCHED)
        groupsRecyclerView.adapter = searchGroupsAdapter
        groupsRecyclerView.layoutManager = LinearLayoutManager(this)
    }


    private fun downloadGroups(dbSearchString: String? = null) {

        val myRef = FirebaseDatabase.getInstance().reference

        var runGroupsQuery = myRef.child(FirebaseString.RUN_GROUPS.value).limitToLast(10)

        if (dbSearchString != null) {

            val searchString = dbSearchString!! //URLEncoder.encode(dbSearchString, "UTF-8")

            runGroupsQuery = runGroupsQuery.orderByChild(FirebaseString.NAME.value).startAt(searchString).endAt(searchString + "\uf8ff")

        } else {

            runGroupsQuery = runGroupsQuery.orderByKey()
            baseRunGroupsList.clear()
        }

        displayedRunGroups.clear()
        searchGroupsAdapter.notifyDataSetChanged()

        runGroupsQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            override fun onDataChange(groupSnapshot: DataSnapshot) {

                for (group: DataSnapshot in groupSnapshot.children) {

                    val groupKey = group.key as String

                    if (!(userRunGroupIds.contains(groupKey))) {

                        val groupName = group.child(FirebaseString.NAME.value).value as String
                        val groupOwner = group.child(FirebaseString.OWNER.value).value as String
                        val groupCode = group.child(FirebaseString.MEMBER_CODE.value).value as String

                        val newRunGroup = RunGroup()
                        newRunGroup.name = groupName
                        newRunGroup.ownerID = groupOwner
                        newRunGroup.fireBaseID = groupKey
                        newRunGroup.memberCode = groupCode

                        val ownerNameDB = myRef.child(FirebaseString.USERS.value).child(groupOwner).child(FirebaseString.NAME.value)

                        ownerNameDB.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(ownerNameSnapshot: DataSnapshot) {

                                newRunGroup.ownerName = ownerNameSnapshot.value as String

                                if (dbSearchString == null) {
                                    baseRunGroupsList.add(newRunGroup)
                                }

                                displayedRunGroups.add(newRunGroup)

                                searchGroupsAdapter.notifyDataSetChanged()
                            }
                        })
                    }
                }
            }
        })
    }


    override fun groupClicked(group: RunGroup, viewHolder: GroupsRecyclerViewAdapter.ViewHolder){

        val inputEditText = EditText(this)
        inputEditText.hint = "Enter Member Code"

        val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        inputEditText.layoutParams = layoutParams

        AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Join This Circle?")
                .setView(inputEditText)
                .setPositiveButton("Join") {_, _ ->

                    val inputCode = inputEditText.text.toString()

                    if (inputCode == group.memberCode) {

                        addUserToGroup(group.fireBaseID)

                    } else {

                        AlertDialog.Builder(this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Incorrect Code")
                                .setMessage("Please Try Again")
                                .setPositiveButton("Ok", null)
                                .show()
                    }

                }
                .setNegativeButton("Cancel", null)
                .setOnDismissListener {
                    viewHolder.background.isEnabled = true
                }
                .show()
    }


    private fun addUserToGroup(groupId: String) {

        val userID = mAuth.currentUser?.uid

        if (userID != null) {

            val myRef = FirebaseDatabase.getInstance().reference

            val userGroupDB = myRef.child(FirebaseString.USERS.value).child(userID).child(FirebaseString.GROUPS.value).child(groupId)

            userGroupDB.setValue(0)
                    .addOnSuccessListener {
                        finish()
                    }
        }
    }


}
