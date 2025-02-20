package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class UserDataViewModel(
    private val firebaseAPI : FirebaseAPI,
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _editUserNameResult = MutableLiveData<Boolean>()
    val editUserNameResult : LiveData<Boolean> =  _editUserNameResult

    fun getUserEmail() : Deferred<String> =
        viewModelScope.async{
            val email = dataStore.getUserEmail()
            return@async email
        }

    fun getUserName() : Deferred<String> =
        viewModelScope.async {
            val name = dataStore.getUserName()
            return@async name
        }

    fun editUserName(name : String) {
        viewModelScope.async(Dispatchers.IO){
            firebaseAPI.editUserName(name).fold(
                onSuccess = {
                    dataStore.updateUserName(name)
                    _editUserNameResult.postValue(true)
                },
                onFailure = {
                    _editUserNameResult.postValue(false)
                }
            )
        }
    }

}