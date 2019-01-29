package com.example.android.interviewtask

import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    val TAG = "ProfileActivity"
    lateinit var database: DatabaseReference

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // inflate the menu
        menuInflater.inflate(R.menu.profile_menu, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val itemId = item?.itemId
        if ( itemId == R.id.sign_out_menu_btn ) {
            Log.d( TAG, "sign out" )
            // sign user out
            val auth = FirebaseAuth.getInstance()
            auth.signOut()

            // close the app
            finishAffinity()
        }
        else if ( itemId == R.id.update_number_menu_btn ) {
            Log.d( TAG, "update phone number" )
            // navigate to update phone number activity
            val intent = Intent(this, UpdateNumberActivity::class.java)
            startActivity(intent)
        }
        else {
            Log.d( TAG, "strange menu item" )
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // get a reference to fire-base real-time database
        database = FirebaseDatabase.getInstance().reference

        // get and show user profile
        getPersonProfile()
    }

    fun getPersonProfile() {
        // show loading progress bar
        showLoadingPB()

        // get to the node where we want to read from
        val testReference = database.child("test")

        // define a listener
        val listener = object : ValueEventListener {
            //specify what to do when data is fetched successfully
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d( TAG, "successfully fetched user profile" )
                // hide loading PB
                hideLoadingPB()

                // Construct a Person object
                val person = snapshot.getValue(Person::class.java)

                // updat UI with person profile info
                updateUI( person )
            }

            // specify what to do when a failure occurs while fetching data
            override fun onCancelled(error: DatabaseError) {
                Log.d( TAG, "Error occurs while fetching user profile" )
                // hide loading PB
                hideLoadingPB()

                // show error message for the user
                showErrorMessage()
            }
        }

        // bind the listener with the node reference
        testReference.addListenerForSingleValueEvent(listener)
    }

    private fun updateUI(person: Person?) {
        if ( person != null ) {
            Log.d( TAG, "Person is not null" )
            person_name.text = person.name
            person_info.text = person.info
        }
        else {
            Log.d( TAG, "Person is null" )
            showErrorMessage()
        }

    }

    private fun showErrorMessage() {
        person_info.text = Constants.ERROR_GETTING_PERSON
        person_info.setTextColor(Color.RED)
    }

    private fun showLoadingPB() {
        profile_loading_PB.visibility = View.VISIBLE
    }

    private fun hideLoadingPB() {
        profile_loading_PB.visibility = View.GONE
    }
}
