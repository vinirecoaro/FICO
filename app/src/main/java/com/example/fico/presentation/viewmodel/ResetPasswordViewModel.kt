package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class ResetPasswordViewModel(
    private val firebaseAPI : FirebaseAPI
) : ViewModel() {

    fun resetPassword(email : String){
        val result = CompletableDeferred<Boolean>()
        viewModelScope.async(Dispatchers.IO){
            firebaseAPI.resetPassword(email).addOnCompleteListener {
                    task ->
                if (task.isSuccessful) {
                    result.complete(true)
                } else {
                    result.complete(false)
                }
            }

            if(result.await()){
                onResetPasswordSuccess()
            }else{
                onResetPasswordFail()
            }
        }
    }

    var onResetPasswordSuccess: () -> Unit = {}
    var onResetPasswordFail: () -> Unit = {}

}