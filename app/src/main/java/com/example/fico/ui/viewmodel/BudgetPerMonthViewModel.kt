package com.example.fico.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Budget
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.withContext

class BudgetPerMonthViewModel : ViewModel() {
    private val firebaseAPI = FirebaseAPI.instance
    private val _budgetPerMonthList = MutableLiveData<List<Budget>>()
    val budgetPerMonthList : LiveData<List<Budget>> = _budgetPerMonthList

    suspend fun getBudgetPerMonth() =
        withContext(viewModelScope.coroutineContext) {
            val budgetList = firebaseAPI.getBudgetPerMonth()
            _budgetPerMonthList.value = budgetList
        }
}