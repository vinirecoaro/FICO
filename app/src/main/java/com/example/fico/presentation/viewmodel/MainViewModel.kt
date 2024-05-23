package com.example.fico.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

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

     fun updateExpensesDatabasePath(){
        viewModelScope.launch {
            FirebaseAPI.instance.updateExpensePerListInformationPath()
            FirebaseAPI.instance.updateDefaultValuesPath()
            FirebaseAPI.instance.updateInformationPerMonthPath()
            FirebaseAPI.instance.updateTotalExpensePath()
        }
    }
}