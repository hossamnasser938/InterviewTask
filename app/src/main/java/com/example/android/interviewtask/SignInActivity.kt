package com.example.android.interviewtask

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.android.interviewtask.Constants.RC_SIGN_IN
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

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

            // check internet connction
            if ( Utils.isNetworkConnected( this ) ) {
                // open Firebase UI to let the user sign in
                openFirebaseAuthUI()
            }
            else {
                showNoInternet()
            }
        }
        else {  // user is already signed in
            Log.d( TAG, "current user is not null" )
            // navigate to profile activity
            navigateToProfile()
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

                // navigate to profile activity
                navigateToProfile()
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

    private fun navigateToProfile() {
        val intent = Intent(this, ProfileActivity::class.java)
        // clear tasks in back stack to avoid getting back to login activity by hitting back button
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    private fun showNoInternet() {
        sign_in_text.text = getString(R.string.no_internet)
        sign_in_text.setTextColor(Color.RED)
    }
}
