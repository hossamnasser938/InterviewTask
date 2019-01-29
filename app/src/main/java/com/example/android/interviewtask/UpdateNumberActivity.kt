package com.example.android.interviewtask

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_update_number.*

class UpdateNumberActivity : AppCompatActivity() {

    val TAG = "UpdateNumberActivity"

    lateinit var auth: FirebaseAuth
    var currentUser : FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_number)

        auth = FirebaseAuth.getInstance()
        currentUser = auth.currentUser

        handleCheckOldNumberButton()
        handleUpdateNewNumberButton()
    }

    fun handleCheckOldNumberButton() {
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


    fun handleUpdateNewNumberButton() {
        update_new_number_btn.setOnClickListener {
            val numberEntered = new_number_edit_text.text.toString()

            // check if empty
            if( numberEntered.isEmpty() ) {
                showErrorOnNewNumber( R.string.enter_number )
                return@setOnClickListener
            }

            // TODO: follow the process
        }
    }

    private fun stopRespondingToOldNumber() {
        check_old_number_btn.isClickable = false
        old_number_edit_text.isClickable = false
    }

    fun showNewNumberLayout() {
        new_number_layout.visibility = View.VISIBLE
    }

    fun showErrorOnOldNumber( stringId: Int ) {
        old_number_error_text.visibility = View.VISIBLE
        old_number_error_text.text = resources.getString( stringId )
    }

    fun showErrorOnNewNumber( stringId: Int ) {
        new_number_error_text.visibility = View.VISIBLE
        new_number_error_text.text = resources.getString( stringId )
    }

    fun hideErrorOldNumber() {
        old_number_error_text.visibility = View.GONE
    }

    fun hideErrorNewNumber() {
        new_number_error_text.visibility = View.GONE
    }
}
