package com.example.fico.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class VerifyEmailViewModel : ViewModel() {

    private val _isVerified = MutableLiveData<Boolean>()
    val isVerified : LiveData<Boolean> = _isVerified
    private val firebaseAPI = FirebaseAPI.instance

    init{
        viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.stateListener().also {
                val user = firebaseAPI.currentUser()
                _isVerified.value = user?.isEmailVerified == true
            }
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