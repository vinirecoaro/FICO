package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.model.Expense
import com.example.fico.service.FirebaseAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.time.LocalTime

class ExpenseListViewModel: ViewModel() {

    private val firebaseAPI = FirebaseAPI.instance
    private val _expensesLiveData = MutableLiveData<List<Expense>>()
    val expensesLiveData: LiveData<List<Expense>> = _expensesLiveData
    private val _expenseMonthsLiveData = MutableLiveData<List<String>>()
    val expenseMonthsLiveData: LiveData<List<String>> = _expenseMonthsLiveData
    private val _filterLiveData = MutableLiveData<String>()
    val filterLiveData : LiveData<String>
        get() = _filterLiveData

    fun updateFilter(filter : String){
        _filterLiveData.value = filter
    }

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

    fun deleteExpense(expense : Expense){
        viewModelScope.async(Dispatchers.IO) {
            firebaseAPI.deleteExpense(expense)
            val filter = filterLiveData.value.toString()
            getExpenseList(filter)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun undoDeleteExpense(expense: Expense)=
        viewModelScope.async(Dispatchers.IO){
            firebaseAPI.addExpense(expense, inputTime = "")
            getExpenseList(filterLiveData.value.toString())
        }

}
