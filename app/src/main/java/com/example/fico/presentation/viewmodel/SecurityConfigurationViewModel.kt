package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class SecurityConfigurationViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _changeBlockAppState = MutableLiveData<Boolean>()
    val changeBlockAppState : LiveData<Boolean> = _changeBlockAppState
    private val _getBlockAppState = MutableLiveData<Boolean>()
    val getBlockAppState : LiveData<Boolean> = _getBlockAppState

    init {
        getBlockAppStateFromDataStore()
    }

    fun setBlockAppState(state : Boolean){
        _changeBlockAppState.postValue(state)
    }

    fun setBlockAppStateOnDataStore(state : Boolean){
        viewModelScope.async(Dispatchers.IO) {
            dataStore.setBlockAppState(state)
        }
    }

    private fun getBlockAppStateFromDataStore(){
        viewModelScope.async(Dispatchers.IO) {
            val state = dataStore.getBlockAppState()
            _getBlockAppState.postValue(state)
        }
    }

}