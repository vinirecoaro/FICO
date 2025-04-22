package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fico.DataStoreManager

class SecurityConfigurationViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _blockAppState = MutableLiveData<Boolean>()
    private val blockAppState : LiveData<Boolean> = _blockAppState

    fun setBlockAppState(state : Boolean){
        _blockAppState.postValue(state)
    }
}