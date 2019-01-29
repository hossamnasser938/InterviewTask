package com.example.android.interviewtask

import android.graphics.Color
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
        handleVerifyCodeButton()
    }

    override fun onStart() {
        super.onStart()
        Log.d( TAG, "on start" )
        Log.d( TAG, "verification in progress $verificationInProgress and new number $newNumber" )

        // check if verification is in progress
        if( verificationInProgress ) {
            showVerificationCodeLayout()
            verifyNumber( newNumber!! )
        }
        else {
            showOldNumberLayout()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Log.d( TAG, "on save instance state" )

        if ( verificationInProgress ) {
            Log.d( TAG, "verification in progress $verificationInProgress" )
            outState?.putBoolean( Constants.VERRIFICATION_IN_PROGRESS, verificationInProgress )
        }

        if ( newNumber != null ) {
            Log.d( TAG, "new number $newNumber" )
            outState?.putString( Constants.NEW_NUMBER, newNumber )
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d( TAG, "on restore instance state" )

        if ( savedInstanceState != null ) {
            Log.d( TAG, "saved instance state is not null" )
            if ( savedInstanceState.containsKey(Constants.VERRIFICATION_IN_PROGRESS) ) {
                verificationInProgress = savedInstanceState.getBoolean(Constants.VERRIFICATION_IN_PROGRESS)
                Log.d( TAG, "saved instance state contains verification in progress $verificationInProgress" )
            }

            if ( savedInstanceState.containsKey(Constants.NEW_NUMBER) ) {
                newNumber = savedInstanceState.getString(Constants.NEW_NUMBER)
                Log.d( TAG, "saved instance state contains new number $newNumber" )
            }
        }
    }

    private fun handleCheckOldNumberButton() {
        check_old_number_btn.setOnClickListener {
            hideErrorOldNumber()

            val numberEntered = old_number_edit_text.text.toString().trim()

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
            }
            else {
                showErrorOnOldNumber( R.string.wrong_number )
            }
        }
    }


    private fun handleUpdateNewNumberButton() {
        update_new_number_btn.setOnClickListener {
            hideErrorNewNumber()

            val numberEntered = new_number_edit_text.text.toString().trim()

            // check if empty
            if( numberEntered.isEmpty() ) {
                showErrorOnNewNumber( R.string.enter_number )
                return@setOnClickListener
            }

            val currentUserNumber = currentUser?.phoneNumber
            val currentUserNumberWithoutKey = currentUserNumber?.substring(2)

            // Compare entered and old user numbers
            if( numberEntered.equals( currentUserNumber, false ) or
                    numberEntered.equals( currentUserNumberWithoutKey, false ) ) {
                showErrorOnNewNumber( R.string.the_same_old_number )
                return@setOnClickListener
            }

            newNumber = numberEntered
            verifyNumber( newNumber!! )
        }
    }

    private fun verifyNumber( number : String ) {
        // define the callback
        verificationCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
                Log.d( TAG, "onVerificationCompleted" )
                if ( credential != null ) {
                    performUpdate(credential)
                }
                else {
                    hideLodingPB()
                    showFailureUpdate()
                }
            }

            override fun onVerificationFailed(p0: FirebaseException?) {
                Log.d( TAG, "onVerificationFailed" )
                hideLodingPB()
                showFailureUpdate()
            }

            override fun onCodeSent(id: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                super.onCodeSent(id, token)
                Log.d( TAG, "onCodeSent" )
                verificationId = id
                resendingToken = token

                // set the flag to reflect the progress state
                verificationInProgress = true

                hideLodingPB()

                showVerificationCodeLayout()
            }
        }

        // send verification code
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                number,
                60,               // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this,             // Activity (for callback binding)
                verificationCallback) // OnVerificationStateChangedCallbacks

        showLodingPB()
    }

    private fun handleVerifyCodeButton() {
        verify_verifiction_code_btn.setOnClickListener {
            showLodingPB()

            hideErrorVerificationCode()

            val codeEntered = verification_code_edit_text.text.toString()

            // check if empty
            if( codeEntered.isEmpty() ) {
                showErrorOnVerificationCode( R.string.enter_number )
            }

            // construct the credential
            val phoneAuthCredential = PhoneAuthProvider.getCredential(verificationId!!, codeEntered)

            performUpdate(phoneAuthCredential)
        }
    }

    private fun performUpdate(phoneAuthCredential: PhoneAuthCredential) {
        currentUser?.updatePhoneNumber(phoneAuthCredential)?.addOnSuccessListener {
            Log.d(TAG, "successful update")
            hideLodingPB()
            showSuccessUpdate()
        }?.addOnFailureListener {
            Log.d(TAG, "failed to update")
            hideLodingPB()
            showFailureUpdate()
        }
    }

    private fun showOldNumberLayout() {
        old_number_layout.visibility = View.VISIBLE
        new_number_layout.visibility = View.GONE
        verification_code_layout.visibility = View.GONE
    }

    private fun showNewNumberLayout() {
        new_number_layout.visibility = View.VISIBLE
        old_number_layout.visibility = View.GONE
        verification_code_layout.visibility = View.GONE
    }

    private fun showVerificationCodeLayout() {
        verification_code_layout.visibility = View.VISIBLE
        old_number_layout.visibility = View.GONE
        new_number_layout.visibility = View.GONE

    }

    private fun showErrorOnOldNumber( stringId: Int ) {
        old_number_error_text.visibility = View.VISIBLE
        old_number_error_text.text = resources.getString( stringId )
    }

    private fun showErrorOnNewNumber( stringId: Int ) {
        new_number_error_text.visibility = View.VISIBLE
        new_number_error_text.text = resources.getString( stringId )
    }

    private fun showErrorOnVerificationCode( stringId: Int ) {
        verification_code_error_text.visibility = View.VISIBLE
        verification_code_error_text.text = resources.getString( stringId )
    }

    private fun hideErrorOldNumber() {
        old_number_error_text.visibility = View.GONE
    }

    fun hideErrorNewNumber() {
        new_number_error_text.visibility = View.GONE
    }

    fun hideErrorVerificationCode() {
        verification_code_error_text.visibility = View.GONE
    }

    private fun showSuccessUpdate() {
        showErrorOnVerificationCode( R.string.success_update )
        verification_code_error_text.setTextColor( Color.GREEN )
    }

    private fun showFailureUpdate() {
        showErrorOnVerificationCode( R.string.failure_update )
        verification_code_error_text.setTextColor( Color.RED )
    }

    private fun showLodingPB() {
        update_PB.visibility = View.VISIBLE
    }

    private fun hideLodingPB() {
        update_PB.visibility = View.GONE
    }
}
