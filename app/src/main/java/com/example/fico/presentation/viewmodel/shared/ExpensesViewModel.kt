package com.example.fico.presentation.viewmodel.shared

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.model.Expense
import com.example.fico.model.InformationPerMonthExpense
import kotlinx.coroutines.async

class ExpensesViewModel(private val dataStore : DataStoreManager,private val firebaseAPI : FirebaseAPI ) : ViewModel() {

    fun getExpenseList(filter : String = "") {
        viewModelScope.async{
            val expenses = firebaseAPI.observeExpenseList(filter).await()
            dataStore.updateAndResetExpenseList(expenses)
        }
    }

    fun getExpenseMonths(){
        viewModelScope.async {
            val expenseMonths = firebaseAPI.getExpenseMonths(false)
            dataStore.updateAndResetExpenseMonths(expenseMonths)
        }
    }

    fun getExpenseInfoPerMonth(){
        viewModelScope.async {
            val expenseInfoPerMonth = firebaseAPI.getInformationPerMonth().await()
            dataStore.updateAndResetInfoPerMonthExpense(expenseInfoPerMonth)
        }
    }

}