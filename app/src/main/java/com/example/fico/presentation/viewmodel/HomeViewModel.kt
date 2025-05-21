package com.example.fico.presentation.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fico.utils.constants.StringConstants

class HomeViewModel : ViewModel() {
    private val _transactionType = MutableLiveData<String>(StringConstants.DATABASE.EXPENSE)

    fun setTransactionType(type: String) {
        _transactionType.value = type
    }

    fun getTransactionType(): String? {
        return _transactionType.value
    }

}