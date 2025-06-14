package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.model.CreditCard
import kotlinx.coroutines.Deferred
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
        getPayWithCreditCardSwitchState()
    }

    private fun getPayWithCreditCardSwitchState(){
        viewModelScope.async(Dispatchers.IO) {
            val state = dataStore.getPayWithCreditCardSwitchState()
            _payWithCreditCardSwitchInitialState.postValue(state)
        }
    }

    fun setPayWithCreditCardSwitchState(state : Boolean){
        viewModelScope.async(Dispatchers.IO) {
            dataStore.setPayWithCreditCardSwitchState(state)
        }
    }

    fun getCreditCardList(){
        viewModelScope.launch(Dispatchers.IO) {
            val creditCardList = dataStore.getCreditCardList()
            _getCreditCardList.postValue(creditCardList)
        }
    }

    fun getDefaultCreditCardId(): Deferred<String> {
        return viewModelScope.async(Dispatchers.IO) {
            dataStore.getDefaultCreditCardId()
        }
    }

}