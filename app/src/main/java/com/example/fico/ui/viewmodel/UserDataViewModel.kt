package com.example.fico.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class UserDataViewModel : ViewModel() {

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

    fun editUserName(name : String) : Deferred<Boolean> {
        return viewModelScope.async(Dispatchers.IO){
            firebaseAPI.editUserName(name)
        }
    }

}