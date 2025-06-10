package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.model.CreditCard
import com.example.fico.repositories.CreditCardRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class CreditCardConfigurationViewModel(
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _getCreditCardList = MutableLiveData<List<CreditCard>>()
    val getCreditCardList : LiveData<List<CreditCard>> = _getCreditCardList
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

    fun getCreditCardList(){
        viewModelScope.launch(Dispatchers.IO) {
            val creditCardList = dataStore.getCreditCardList()
            _getCreditCardList.postValue(creditCardList)
        }
    }
}