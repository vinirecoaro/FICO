package com.example.fico.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fico.service.FirebaseAPI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase

class VerifyEmailViewModel : ViewModel() {

    private val _isVerified = MutableLiveData<Boolean>()
    val isVerified : LiveData<Boolean> = _isVerified
    private val auth = FirebaseAuth.getInstance()

    init{
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _isVerified.value = user?.isEmailVerified ?: false
        }
    }

}