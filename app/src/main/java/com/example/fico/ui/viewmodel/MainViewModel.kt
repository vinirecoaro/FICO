package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class MainViewModel : ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance

    fun getUserEmail() : Deferred<String> =
        viewModelScope.async{
        val email = firebaseAPI.getUserEmail()
        return@async email
    }

    fun getUserName() : Deferred<String> =
        viewModelScope.async {
        val name = firebaseAPI.getUserName()
        return@async name
    }
}