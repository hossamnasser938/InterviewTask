package com.example.android.interviewtask

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_update_number.*
import java.util.concurrent.TimeUnit

class UpdateNumberActivity : AppCompatActivity() {

    val TAG = "UpdateNumberActivity"

    lateinit var auth: FirebaseAuth
    var currentUser: FirebaseUser? = null

    var newNumber: String? = null

    lateinit var verificationCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    var verificationId: String? = null
    var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    var verificationInProgress: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_number)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        handleCheckOldNumberButton()
        handleUpdateNewNumberButton()
    }

    override fun onStart() {
        super.onStart()

        // check if verification is in progress
        if( verificationInProgress ) {
            verifyNumber( newNumber!! )
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        if ( verificationInProgress ) {
            outState?.putBoolean( Constants.VERRIFICATION_IN_PROGRESS, verificationInProgress )
        }

        if ( newNumber != null ) {
            outState?.putString( Constants.NEW_NUMBER, newNumber )
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        if ( savedInstanceState != null ) {
            if ( savedInstanceState.containsKey(Constants.VERRIFICATION_IN_PROGRESS) ) {
                verificationInProgress = savedInstanceState.getBoolean(Constants.VERRIFICATION_IN_PROGRESS)
            }

            if ( savedInstanceState.containsKey(Constants.NEW_NUMBER) ) {
                newNumber = savedInstanceState.getString(Constants.NEW_NUMBER)
            }
        }
    }

    private fun handleCheckOldNumberButton() {
        check_old_number_btn.setOnClickListener {
            val numberEntered = old_number_edit_text.text.toString()

            // check if empty
            if( numberEntered.isEmpty() ) {
                showErrorOnOldNumber( R.string.enter_number )
                return@setOnClickListener
            }

            val currentUserNumber = currentUser?.phoneNumber
            val currentUserNumberWithoutKey = currentUserNumber?.substring(2)

            // Compare entered and user numbers
            if( numberEntered.equals( currentUserNumber, false ) or
                    numberEntered.equals( currentUserNumberWithoutKey, false ) ) {
                // show fields for new number
                showNewNumberLayout()

                hideErrorOldNumber()

                stopRespondingToOldNumber()
            }
            else {
                showErrorOnOldNumber( R.string.wrong_number )
            }
        }
    }


    private fun handleUpdateNewNumberButton() {
        update_new_number_btn.setOnClickListener {
            val numberEntered = new_number_edit_text.text.toString()

            // check if empty
            if( numberEntered.isEmpty() ) {
                showErrorOnNewNumber( R.string.enter_number )
                return@setOnClickListener
            }

            newNumber = numberEntered
            verifyNumber( newNumber!! )
        }
    }

    private fun verifyNumber( number : String ) {
        // set the flag to reflect the progress state
        verificationInProgress = true

        // verify number
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,               // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this,             // Activity (for callback binding)
                verificationCallback) // OnVerificationStateChangedCallbacks

        // define the callback
        verificationCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
                Log.d( TAG, "onVerificationCompleted" )
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                Log.d( TAG, "onVerificationFailed" )
            }

            override fun onCodeSent(id: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(id, token)
                Log.d( TAG, "onCodeSent" )
                verificationId = id
                resendingToken = token
            }
        }
    }

    private fun stopRespondingToOldNumber() {
        check_old_number_btn.isClickable = false
        old_number_edit_text.isClickable = false
    }

    private fun showNewNumberLayout() {
        new_number_layout.visibility = View.VISIBLE
    }

    private fun showErrorOnOldNumber( stringId: Int ) {
        old_number_error_text.visibility = View.VISIBLE
        old_number_error_text.text = resources.getString( stringId )
    }

    private fun showErrorOnNewNumber( stringId: Int ) {
        new_number_error_text.visibility = View.VISIBLE
        new_number_error_text.text = resources.getString( stringId )
    }

    private fun hideErrorOldNumber() {
        old_number_error_text.visibility = View.GONE
    }

    fun hideErrorNewNumber() {
        new_number_error_text.visibility = View.GONE
    }
}
