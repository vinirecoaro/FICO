package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fico.service.FirebaseAPI
import com.google.firebase.ktx.Firebase

class VerifyEmailViewModel : ViewModel() {

    private val user = FirebaseAPI.getInstance().currentUser()

    fun verifyEmail() : Boolean{
        return user?.isEmailVerified == true
    }

}