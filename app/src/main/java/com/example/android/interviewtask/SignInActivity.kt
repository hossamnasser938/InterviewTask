package com.example.android.interviewtask

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.android.interviewtask.Constants.RC_SIGN_IN
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    val TAG = "SignInActivity"
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // check if user is signed in or not
        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if ( currentUser == null ) {  // user is not signed in
            Log.d( TAG, "current user is null" )
            // open Firebase UI to let the user sign in
            openFirebaseAuthUI()
        }
        else {  // user is already signed in
            Log.d( TAG, "current user is not null" )
            // TODO: navigate to profile activity
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if ( requestCode == RC_SIGN_IN ) {
            Log.d( TAG, "on activity result using RC_SIGN_IN" )
            val response = IdpResponse.fromResultIntent(data)

            if ( resultCode == Activity.RESULT_OK ) {
                Log.d( TAG, "result code is OK" )
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser

                // TODO: navigate to profile activity
            } else {
                Log.d( TAG, "result code is not OK" )
                // Sign in failed
                openFirebaseAuthUI()
            }
        }
    }

    private fun openFirebaseAuthUI() {
        // Choose authentication providers
        val providers = arrayListOf(AuthUI.IdpConfig.PhoneBuilder().build())

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN)
    }
}
