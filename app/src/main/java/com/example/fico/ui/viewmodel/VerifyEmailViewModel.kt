package com.example.fico.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fico.service.FirebaseAPI
import com.google.firebase.auth.FirebaseAuth

class VerifyEmailViewModel : ViewModel() {

    private val _isVerified = MutableLiveData<Boolean>()
    val isVerified : LiveData<Boolean> = _isVerified
    private val auth = FirebaseAPI.getInstance()

    init{
        auth.stateListener().also {
            val user = auth.currentUser()
            _isVerified.value = user?.isEmailVerified == true
        }
    }

    fun logoff(){
        auth.logoff()
    }

    fun sendEmailVerificarion(){
        auth.sendEmailVerification()
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