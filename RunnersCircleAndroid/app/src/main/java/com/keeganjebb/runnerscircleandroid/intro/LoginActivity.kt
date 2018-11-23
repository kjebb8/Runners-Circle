package com.keeganjebb.runnerscircleandroid.intro

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.keeganjebb.runnerscircleandroid.home.GroupsActivity
import com.keeganjebb.runnerscircleandroid.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import com.keeganjebb.runnerscircleandroid.support.hideSoftKeyboard


class LoginActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        title = "Log In"

        backgroundImageViewLogin.setOnClickListener {
            hideSoftKeyboard()
        }
    }


    fun loginButtonPressed(view: View) {

        val email = emailEditTextLogin.text.toString()
        val password = passwordEditTextLogin.text.toString()

        hideSoftKeyboard()

        if (email.isNotEmpty() && password.isNotEmpty()) {

            progressBar3.visibility = View.VISIBLE

            view.isEnabled = false

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val loginIntent = Intent(applicationContext, GroupsActivity::class.java)
                    startActivity(loginIntent)

                    progressBar3.visibility = View.INVISIBLE

                } else {

                    progressBar3.visibility = View.INVISIBLE

                    AlertDialog.Builder(this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Log In Failed")
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
                    .setTitle("Log In Failed")
                    .setMessage("Please fill in all fields")
                    .setPositiveButton("Ok") { _, _ ->
//                        showSoftKeyboard()
                    }
                    .show()
        }
    }


}
