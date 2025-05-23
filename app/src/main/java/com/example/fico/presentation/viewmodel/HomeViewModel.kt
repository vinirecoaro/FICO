package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fico.utils.constants.StringConstants

class HomeViewModel : ViewModel() {
    private val _transactionType = MutableLiveData(StringConstants.DATABASE.EXPENSE)
    private val _arrowState = MutableLiveData(true)
    val arrowState : LiveData<Boolean> = _arrowState

    fun setTransactionType(type: String) {
        _transactionType.value = type
    }

    fun getTransactionType(): String? {
        return _transactionType.value
    }

    fun setArrowState(state: Boolean){
        _arrowState.value = state
    }

}