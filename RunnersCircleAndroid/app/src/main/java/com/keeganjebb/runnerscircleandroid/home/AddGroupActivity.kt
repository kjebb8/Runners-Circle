package com.keeganjebb.runnerscircleandroid.home

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.keeganjebb.runnerscircleandroid.support.FirebaseString
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.support.hideSoftKeyboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_add_group.*


class AddGroupActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_group)

        title = "Create Circle"

        backgroundImageViewAddGroup.setOnClickListener {
            hideSoftKeyboard()
        }
    }


    fun registerCirclePressed(view: View) {

        hideSoftKeyboard()

        if (newGroupNameEditText.text.isNotEmpty() && memberCodeEditText.text.isNotEmpty()) {

            view.isEnabled = false

            val connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected")

            connectedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {

                    val connected = p0.value as? Boolean

                    if (connected != null && connected == true) {
                        addGroup()
                    } else {

                        AlertDialog.Builder(this@AddGroupActivity)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Error")
                                .setMessage("Not Connected to Server")
                                .setPositiveButton("Ok") { _, _ ->
//                                    showSoftKeyboard()
                                }
                                .show()

                        view.isEnabled = true
                    }
                }
            })

        } else {

            AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Invalid Circle")
                    .setMessage("Please fill in all fields")
                    .setPositiveButton("Ok") { _, _ ->
//                        showSoftKeyboard()
                    }
                    .show()
        }
    }


    private fun addGroup() {

        val groupName = newGroupNameEditText.text.toString()
        val memberCode = memberCodeEditText.text.toString()

        val userID = mAuth.currentUser?.uid

        if (userID != null) {

            val myRef = FirebaseDatabase.getInstance().reference

            val runGroupsDB = myRef.child(FirebaseString.RUN_GROUPS.value)

            val newGroupId = runGroupsDB.push().key

            if (newGroupId != null) {

                val newGroupDB = runGroupsDB.child(newGroupId)

                val groupInfoMap = mapOf(
                        FirebaseString.NAME.value to groupName, //URLEncoder.encode(groupName, "UTF-8"),
                        FirebaseString.OWNER.value to userID,
                        FirebaseString.MEMBER_CODE.value to memberCode
                )

                newGroupDB.setValue(groupInfoMap)
                        .addOnSuccessListener {

                            val userGroupDB = myRef.child(FirebaseString.USERS.value).child(userID).child(FirebaseString.GROUPS.value).child(newGroupId)
                            userGroupDB.setValue(0)
                        }
            }
        }

        finish()
    }


}
