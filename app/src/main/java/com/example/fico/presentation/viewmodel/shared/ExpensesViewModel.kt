package com.example.fico.presentation.viewmodel.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.model.Earning
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
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
            if(paymentDay != StringConstants.DEFAULT_MESSAGES.FAIL){
                dataStore.setDefaultPaymentDay(paymentDay)
            }
        }
    }

    fun getDaysForClosingBill() {
        viewModelScope.launch {
            val daysForClosingBill = firebaseAPI.getDaysForClosingBill().await()
            if(daysForClosingBill != StringConstants.DEFAULT_MESSAGES.FAIL){
                dataStore.setDaysForClosingBill(daysForClosingBill)
            }
        }
    }

    fun getEarningsList(){
        viewModelScope.async {
            val earningList = mutableListOf<Earning>()
            val earningListSnapShot = firebaseAPI.getEarningList().await().children
            val earningMonthsList = mutableSetOf<String>()
            for(earning in earningListSnapShot){
                earningList.add(
                    Earning(
                        id = earning.key.toString(),
                        value = earning.child(StringConstants.DATABASE.VALUE).value.toString(),
                        description = earning.child(StringConstants.DATABASE.DESCRIPTION).value.toString(),
                        category = earning.child(StringConstants.DATABASE.CATEGORY).value.toString(),
                        date = earning.child(StringConstants.DATABASE.DATE).value.toString(),
                        inputDateTime = earning.child(StringConstants.DATABASE.INPUT_DATE_TIME).value.toString()
                    )
                )
                earningMonthsList.add(DateFunctions().YYYYmmDDtoYYYYmm(earning.child(StringConstants.DATABASE.DATE).value.toString()))
            }
            dataStore.updateAndResetEarningList(earningList)
            dataStore.updateAndResetEarningMonths(earningMonthsList.toList())
        }
    }

}