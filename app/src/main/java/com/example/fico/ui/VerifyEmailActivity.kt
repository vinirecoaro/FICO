package com.example.fico.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.example.fico.R
import com.example.fico.ui.viewmodel.VerifyEmailViewModel

class VerifyEmailActivity : AppCompatActivity() {

    private val viewModel by viewModels<VerifyEmailViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)
        setUpListeners()
    }

    private fun setUpListeners(){
        viewModel.isVerified.observe(this, Observer {isVerified ->
            if(isVerified){
                startActivity(Intent(this, MainActivity::class.java))
            }
        })
    }

}