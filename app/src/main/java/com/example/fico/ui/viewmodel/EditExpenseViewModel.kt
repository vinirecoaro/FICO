package com.example.fico.ui.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fico.api.ArrangeDataToUpdateToDatabase
import com.example.fico.model.Expense
import com.example.fico.api.FirebaseAPI
import com.example.fico.api.FormatValuesFromDatabase
import com.example.fico.api.FormatValuesToDatabase
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async

class EditExpenseViewModel : ViewModel() {

    val firebaseAPI = FirebaseAPI.instance

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun saveEditExpense(
        oldExpense: Expense,
        price: String,
        description: String,
        category: String,
        date: String,
        installment : Boolean,
        nOfInstallments: Int = 1
    ) : Deferred<Boolean > {
        return viewModelScope.async(Dispatchers.IO) {

            var oldExpenseDate = FormatValuesToDatabase().expenseDate(oldExpense.date)

            var oldExpenseNOfInstallment = 1

            if(installment){
                oldExpenseNOfInstallment = FormatValuesFromDatabase().installmentExpenseNofInstallment(oldExpense.id).toInt()
                oldExpenseDate = FormatValuesToDatabase().expenseDate(FormatValuesFromDatabase().installmentExpenseInitialDate(oldExpense.id,oldExpense.date))
            }

            val oldExpenseFormatted = Expense(
                oldExpense.id,
                oldExpense.price,
                oldExpense.description,
                oldExpense.category,
                oldExpenseDate
            )

            val removeFromExpenseList = ArrangeDataToUpdateToDatabase().removeFromExpenseList(oldExpenseFormatted, viewModelScope).await()

            val newExpensePrice = FormatValuesToDatabase().expensePrice(price, nOfInstallments)
            val newExpenseDate = FormatValuesToDatabase().expenseDate(date)

            val newExpense = Expense(id = "", newExpensePrice, description, category, newExpenseDate)

            val updatedTotalExpense = ArrangeDataToUpdateToDatabase().calculateUpdatedTotalExpense(newExpense.price, nOfInstallments, viewModelScope, oldExpenseFormatted.price, oldExpenseNOfInstallment).await()

            val updatedInformationPerMonth = ArrangeDataToUpdateToDatabase().addToInformationPerMonth(newExpense, installment, nOfInstallments, viewModelScope, true, oldExpenseFormatted).await()

            val expenseList = ArrangeDataToUpdateToDatabase().addToExpenseList(newExpense, installment, nOfInstallments)

            firebaseAPI.editExpense(expenseList,nOfInstallments, updatedTotalExpense,updatedInformationPerMonth, removeFromExpenseList, oldExpenseNOfInstallment)
        }
    }

}