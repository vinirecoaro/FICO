package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class DefaultPaymentDateConfigurationViewModel(
    private val firebaseAPI: FirebaseAPI,
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _setDefaultBudget = MutableLiveData<Boolean>()
    val setDefaultBudgetLiveData: LiveData<Boolean> = _setDefaultBudget
    private val _paymentDateSwitchInitialState = MutableLiveData<Boolean>()
    val paymentDateSwitchInitialStateLiveData: LiveData<Boolean> = _paymentDateSwitchInitialState

    init {
        getPaymentDateSwitchState()
    }

    fun setDefaultPaymentDay(date: String) {
        viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.setDefaultPaymentDay(date).fold(
                onSuccess = {
                    dataStore.setDefaultPaymentDay(date)
                    _setDefaultBudget.postValue(true)
                },
                onFailure = {
                    _setDefaultBudget.postValue(false)
                }
            )

        }
    }

    private fun getPaymentDateSwitchState(){
        viewModelScope.async(Dispatchers.IO) {
            val state = dataStore.getPaymentDateSwitchInitialState()
            _paymentDateSwitchInitialState.postValue(state)
        }
    }

    fun setPaymentDateSwitchInitialState(state : Boolean){
        viewModelScope.async(Dispatchers.IO) {
            dataStore.setPaymentDateSwitchInitialState(state)
        }
    }
}