package com.keeganjebb.runnerscircleandroid.intro

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.keeganjebb.runnerscircleandroid.home.GroupsActivity
import com.keeganjebb.runnerscircleandroid.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_welcome.*


class WelcomeActivity : AppCompatActivity() {

    private val mAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        title = "Welcome to Runners' Circle"
    }


    override fun onStart() {
        super.onStart()

        if (mAuth.currentUser != null) {

            val userExistsIntent = Intent(applicationContext, GroupsActivity::class.java)
            startActivity(userExistsIntent)
        }

        goToLoginButton.isEnabled = true
        goToRegisterButton.isEnabled = true
    }


    fun goToRegisterButtonPressed(view: View) {

        view.isEnabled = false

        val registerIntent = Intent(applicationContext, RegisterActivity::class.java)
        startActivity(registerIntent)
    }


    fun goToLoginButtonPressed(view: View) {

        view.isEnabled = false

        val loginIntent = Intent(applicationContext, LoginActivity::class.java)
        startActivity(loginIntent)
    }


}
