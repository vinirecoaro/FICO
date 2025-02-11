package com.example.fico.presentation.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MainViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val dataStore : DataStoreManager
) : ViewModel() {

    fun getUserEmail() : Deferred<String> =
        viewModelScope.async{
        val email = firebaseAPI.getUserEmail()
        dataStore.updateUserEmail(email)
        return@async email
    }

    fun getUserEmailDataStore() : Deferred<String> =
        viewModelScope.async {
        val email = dataStore.getUserEmail()
        return@async email
    }

    fun getUserName() : Deferred<String> =
        viewModelScope.async {
        val name = firebaseAPI.getUserName()
        dataStore.updateUserName(name)
        return@async name
    }

    fun getUserNameDataStore() : Deferred<String> =
        viewModelScope.async {
            val name = dataStore.getUserName()
            return@async name
        }

}