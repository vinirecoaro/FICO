package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class CreditCardConfigurationViewModel(
    private val firebaseAPI: FirebaseAPI,
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _setDefaultPaymentDate = MutableLiveData<Boolean>()
    val setDefaultPaymentDateLiveData: LiveData<Boolean> = _setDefaultPaymentDate
    private val _payWithCreditCardSwitchInitialState = MutableLiveData<Boolean>()
    val payWithCreditCardSwitchInitialState: LiveData<Boolean> = _payWithCreditCardSwitchInitialState

    init {
        getPaymentDateSwitchState()
    }

    //TODO Change to credit card
    private fun getPaymentDateSwitchState(){
        viewModelScope.async(Dispatchers.IO) {
            val state = dataStore.getPaymentDateSwitchInitialState()
            _payWithCreditCardSwitchInitialState.postValue(state)
        }
    }

    //TODO Change to credit card
    fun setPaymentDateSwitchInitialState(state : Boolean){
        viewModelScope.async(Dispatchers.IO) {
            dataStore.setPaymentDateSwitchInitialState(state)
        }
    }
}