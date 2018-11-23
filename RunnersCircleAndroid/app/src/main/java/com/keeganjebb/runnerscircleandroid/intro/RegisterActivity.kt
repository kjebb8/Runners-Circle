package com.keeganjebb.runnerscircleandroid.intro

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.keeganjebb.runnerscircleandroid.support.FirebaseString
import com.keeganjebb.runnerscircleandroid.home.GroupsActivity
import com.keeganjebb.runnerscircleandroid.R
import com.keeganjebb.runnerscircleandroid.support.hideSoftKeyboard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*



class RegisterActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        title = "Register"

        backgroundImageViewRegister.setOnClickListener {
            hideSoftKeyboard()
        }
    }


    fun registerButtonPressed(view: View) {

        hideSoftKeyboard()

        val name = nameEditTextRegister.text.toString()
        val email = emailEditTextRegister.text.toString()
        val password = passwordEditTextRegister.text.toString()

        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {

            progressBar2.visibility = View.VISIBLE

            view.isEnabled = false

            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val newUserID = mAuth.currentUser?.uid

                    if (newUserID != null) {

                        val myRef = FirebaseDatabase.getInstance().reference

                        val userDB = myRef.child(FirebaseString.USERS.value).child(newUserID)

                        val newUserMap = mapOf(
                                FirebaseString.NAME.value to name,
                                FirebaseString.EMAIL.value to email
                        )

                        userDB.setValue(newUserMap)
                                .addOnSuccessListener {

                                    val registerIntent = Intent(applicationContext, GroupsActivity::class.java)
                                    startActivity(registerIntent)

                                    progressBar2.visibility = View.INVISIBLE
                                }
                    }

                } else {

                    progressBar2.visibility = View.INVISIBLE

                    AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Registration Failed")
                            .setMessage(task.exception?.message.toString())
                            .setPositiveButton("Ok") { _, _ ->
//                                showSoftKeyboard()
                            }
                            .show()

                    view.isEnabled = true
                }
            }

        } else {

            AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Registration Failed")
                    .setMessage("Please fill in all fields")
                    .setPositiveButton("Ok") { _, _ ->
//                        showSoftKeyboard()
                    }
                    .show()
        }
    }


}
