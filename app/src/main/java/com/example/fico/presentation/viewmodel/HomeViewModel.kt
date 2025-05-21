package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.fico.utils.constants.StringConstants

class HomeViewModel : ViewModel() {
    private val _transactionType = MutableLiveData<String>(StringConstants.DATABASE.EXPENSE)
    val transactionType : LiveData<String> = _transactionType

    fun setTransactionType(type: String) {
        _transactionType.value = type
    }

    fun getTransactionType(): String? {
        return _transactionType.value
    }

}