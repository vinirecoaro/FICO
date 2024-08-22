package com.example.fico.presentation.viewmodel.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.util.constants.AppConstants
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ExpensesViewModel(
    private val dataStore : DataStoreManager,
    private val firebaseAPI : FirebaseAPI
) : ViewModel() {

    fun getExpenseList(filter : String = "") {
        viewModelScope.async{
            val expenses = firebaseAPI.getExpenseList(filter).await()
            dataStore.updateAndResetExpenseList(expenses)
        }
    }

    fun getExpenseMonths(){
        viewModelScope.async {
            val expenseMonths = firebaseAPI.getExpenseMonths()
            dataStore.updateAndResetExpenseMonths(expenseMonths)
        }
    }

    fun getExpenseInfoPerMonth(){
        viewModelScope.async {
            val expenseInfoPerMonth = firebaseAPI.getInformationPerMonth().await()
            dataStore.updateAndResetInfoPerMonthExpense(expenseInfoPerMonth)
        }
    }

    fun getTotalExpense(){
        viewModelScope.async {
            val totalExpense = firebaseAPI.getTotalExpense().await()
            dataStore.updateTotalExpense(totalExpense)
        }
    }

    fun getDefaultBudget(){
        viewModelScope.async {
            val defaultBudget = firebaseAPI.getDefaultBudget().await()
            dataStore.updateDefaultBudget(defaultBudget)
        }
    }

    fun getDefaultPaymentDay() {
        viewModelScope.launch {
            val paymentDay = firebaseAPI.getDefaultPaymentDay().await()
            if(paymentDay != AppConstants.DEFAULT_MESSAGES.FAIL){
                dataStore.setDefaultPaymentDay(paymentDay)
            }
        }
    }

}