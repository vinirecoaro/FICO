package com.example.fico.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fico.service.FirebaseAPI
import com.google.firebase.auth.FirebaseAuth

class VerifyEmailViewModel : ViewModel() {

    private val _isVerified = MutableLiveData<Boolean>()
    val isVerified : LiveData<Boolean> = _isVerified
    private val firebaseAPI = FirebaseAPI.instance

    init{
        firebaseAPI.also {
            val user = FirebaseAPI.auth.currentUser
            _isVerified.value = user?.isEmailVerified == true
        }
    }

    fun logoff(){
        firebaseAPI.logoff()
    }

    fun sendEmailVerificarion(){
        firebaseAPI.sendEmailVerification()
            ?.addOnCompleteListener{
                onSendEmailSuccess()
            }
            ?.addOnFailureListener{
                onSendEmailFailure()
            }
    }

    var onSendEmailSuccess: () -> Unit = {}
    var onSendEmailFailure: () -> Unit = {}

}