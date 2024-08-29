package com.example.fico.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.shared.constants.StringConstants
import kotlinx.coroutines.async

class ExpenseConfigurationViewModel(
    private val firebaseAPI: FirebaseAPI,
    private val dataStore : DataStoreManager
) : ViewModel() {

    private val _setDefaultBudget = MutableLiveData<Boolean>()
    val setDefaultBudgetLiveData: LiveData<Boolean> = _setDefaultBudget

    val configurationList: MutableList<String> = mutableListOf(
        StringConstants.EXPENSE_CONFIGURATION_LIST.BUDGET,
        StringConstants.EXPENSE_CONFIGURATION_LIST.DEFAULT_PAYMENT_DATE
    )

    fun setDefaultPaymentDay(date: String) {
        viewModelScope.async {
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



}