package com.example.fico.presentation.viewmodel.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.DataStoreManager
import com.example.fico.api.FirebaseAPI
import com.example.fico.model.Earning
import com.example.fico.model.RecurringTransaction
import com.example.fico.utils.DateFunctions
import com.example.fico.utils.constants.StringConstants
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class ExpensesViewModel(
    private val dataStore : DataStoreManager,
    private val firebaseAPI : FirebaseAPI
) : ViewModel() {

    /*fun getRecurringExpensesList(){
        viewModelScope.async {
            val recurringExpensesList = mutableListOf<RecurringTransaction>()
            val recurringExpensesListSnapShot = firebaseAPI.getRecurringExpensesList().await().children
            for(recurringExpense in recurringExpensesListSnapShot){
                recurringExpensesList.add(
                    RecurringTransaction(
                        id = recurringExpense.key.toString(),
                        price = recurringExpense.child(StringConstants.DATABASE.PRICE).value.toString(),
                        description = recurringExpense.child(StringConstants.DATABASE.DESCRIPTION).value.toString(),
                        category = recurringExpense.child(StringConstants.DATABASE.CATEGORY).value.toString(),
                        day = recurringExpense.child(StringConstants.DATABASE.DAY).value.toString(),
                        inputDateTime = recurringExpense.child(StringConstants.DATABASE.INPUT_DATE_TIME).value.toString(),
                        type = recurringExpense.child(StringConstants.DATABASE.TYPE).value.toString()
                    )
                )
            }
            dataStore.updateAndResetRecurringExpensesList(recurringExpensesList)
        }
    }*/

}