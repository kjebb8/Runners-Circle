package com.keeganjebb.runnerscircleandroid.home

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.keeganjebb.runnerscircleandroid.support.FirebaseString
import com.keeganjebb.runnerscircleandroid.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_profile.*


class ProfileActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        title = "Profile"

        loadUserData()
    }


    private fun loadUserData() {

        val userID = mAuth.currentUser?.uid

        if (userID != null) {

            val myRef = FirebaseDatabase.getInstance().reference

            val userDB = myRef.child(FirebaseString.USERS.value).child(userID)

            userDB.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(userSnapshot: DataSnapshot) {

                    val userName = userSnapshot.child(FirebaseString.NAME.value).value as? String
                    val userEmail = userSnapshot.child(FirebaseString.EMAIL.value).value as? String
                    val userGroups = userSnapshot.child(FirebaseString.GROUPS.value)

                    var totalCircles = 0
                    var totalRuns = 0

                    for (group: DataSnapshot in userGroups.children) {

                        totalCircles++
                        totalRuns += (group.value as Long).toInt()
                    }

                    nameTextView.text = "Name: " + (userName ?: "")
                    emailTextView.text = "Email: " + (userEmail ?: "")
                    totalCirclesTextView.text = "Total Circles: $totalCircles"
                    totalRunsTextView.text = "Total Runs: $totalRuns"
                }
            })
        }
    }


}
