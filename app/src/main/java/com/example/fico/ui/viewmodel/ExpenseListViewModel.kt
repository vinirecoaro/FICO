package com.example.fico.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.async

class ExpenseListViewModel: ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance
    private val _expensesLiveData = MutableLiveData<List<Expense>>()
    val expensesLiveData: LiveData<List<Expense>> = _expensesLiveData
    private val _expenseMonthsLiveData = MutableLiveData<List<String>>()
    val expenseMonthsLiveData: LiveData<List<String>> = _expenseMonthsLiveData

    fun getExpenseList(filter : String) {
        viewModelScope.async{
            val expenses = firebaseAPI.getExpenseList(filter)
            val sortedExpenses = expenses.sortedByDescending { it.id }
            _expensesLiveData.value = sortedExpenses
        }
    }

    fun getExpenseMonths(){
        viewModelScope.async {
            var expenseMonths = firebaseAPI.getExpenseMonths()
            _expenseMonthsLiveData.value = expenseMonths
        }
    }

}
